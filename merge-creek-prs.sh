#!/bin/bash
# Fetches all open creek-service PR notifications, re-runs failed CI checks,
# and squash-merges PRs that pass. Processes each repo sequentially, all repos in parallel.
# Logs per-PR results to $LOG_DIR.
#
# Usage: ./merge-creek-prs.sh
# Requires: gh CLI authenticated with creek-service org access

LOG_DIR="/tmp/creek_pr_logs"
NOTIFS_FILE="/tmp/creek_pr_notifs.tsv"
TIMEOUT_SECS=3600  # max wait per PR for CI to pass

mkdir -p "$LOG_DIR"

fetch_notifications() {
  echo "Fetching open creek-service PR notifications..."
  gh api "notifications?all=false" --paginate \
    | jq -r '.[] | select(.subject.type == "PullRequest") | select(.repository.full_name | startswith("creek-service/")) | .repository.full_name + "\t" + .id + "\t" + .subject.url + "\t" + .subject.title' \
    | sort > "$NOTIFS_FILE"
  echo "Found $(wc -l < "$NOTIFS_FILE" | tr -d ' ') PR notifications across $(cut -f1 "$NOTIFS_FILE" | sort -u | wc -l | tr -d ' ') repos."
}

process_pr() {
  local repo="$1" notif_id="$2" pr_url="$3" title="$4"
  local pr_num=$(basename "$pr_url")
  local log="$LOG_DIR/${repo//\//_}_${pr_num}.log"

  log() { echo "[$(date '+%H:%M:%S')] [$repo #$pr_num] $*" >> "$log"; }

  log "START: $title"

  # Skip non-open PRs and mark notification done
  local pr_state=$(gh api "$pr_url" --jq '.state' 2>/dev/null)
  if [ "$pr_state" != "open" ]; then
    log "PR is $pr_state, skipping"
    gh api --method DELETE "notifications/threads/$notif_id" >/dev/null 2>&1
    return 0
  fi

  local head_sha=$(gh api "$pr_url" --jq '.head.sha' 2>/dev/null)

  # Re-run any failed/cancelled workflow runs
  local rerun_count=0
  while IFS= read -r run_id; do
    log "Re-running failed run $run_id"
    gh api --method POST "repos/$repo/actions/runs/$run_id/rerun-failed-jobs" --silent 2>/dev/null || \
    gh api --method POST "repos/$repo/actions/runs/$run_id/rerun" --silent 2>/dev/null || true
    rerun_count=$((rerun_count + 1))
  done < <(gh api "repos/$repo/actions/runs?head_sha=$head_sha&per_page=100" \
    --jq '.workflow_runs[] | select(.conclusion != null and .conclusion != "success" and .conclusion != "skipped" and .conclusion != "neutral") | .id' 2>/dev/null)

  # Give re-runs time to register before first check
  if [ "$rerun_count" -gt 0 ]; then
    sleep 60
  fi

  # Poll until checks complete or timeout
  local waited=0
  while [ $waited -lt $TIMEOUT_SECS ]; do
    local checks=$(gh pr checks "$pr_num" --repo "$repo" 2>/dev/null)
    local pending=$(echo "$checks" | grep -cE '\s+(pending|in_progress|queued)\s' || true)
    local failing=$(echo "$checks" | grep -cE '\s+fail\s' || true)

    if [ "$pending" -gt 0 ]; then
      log "Waiting... ($pending pending, ${waited}s elapsed)"
      sleep 30
      waited=$((waited + 30))
      continue
    fi

    if [ "$failing" -eq 0 ]; then
      log "All checks passed! Merging..."
      local merge_out
      if merge_out=$(gh pr merge "$pr_num" --repo "$repo" --squash --delete-branch 2>&1); then
        log "Merged successfully"
        gh api --method DELETE "notifications/threads/$notif_id" >/dev/null 2>&1
        log "Notification marked as done"
      elif echo "$merge_out" | grep -q "not up to date"; then
        log "Branch behind base, updating..."
        if gh api --method PUT "repos/$repo/pulls/$pr_num/update-branch" --silent 2>/dev/null; then
          sleep 30
          if merge_out=$(gh pr merge "$pr_num" --repo "$repo" --squash --delete-branch 2>&1); then
            log "Merged after update"
            gh api --method DELETE "notifications/threads/$notif_id" >/dev/null 2>&1
            log "Notification marked as done"
          else
            log "Merge failed after update: $merge_out"
          fi
        else
          log "Branch update failed"
        fi
      else
        log "Merge failed: $merge_out"
      fi
    else
      log "Checks still failing, skipping (${failing} failures)"
    fi
    return 0
  done

  log "TIMEOUT after ${waited}s"
}

process_repo() {
  local repo="$1"
  echo "=== Starting repo: $repo ===" >> "$LOG_DIR/summary.log"
  while IFS=$'\t' read -r r notif_id pr_url title; do
    process_pr "$repo" "$notif_id" "$pr_url" "$title"
  done < <(grep "^${repo}	" "$NOTIFS_FILE")
  echo "=== Done with repo: $repo ===" >> "$LOG_DIR/summary.log"
}

export -f process_pr process_repo
export LOG_DIR NOTIFS_FILE TIMEOUT_SECS

fetch_notifications

> "$LOG_DIR/summary.log"
repos=$(cut -f1 "$NOTIFS_FILE" | sort -u)
echo "$repos" | xargs -P 16 -I {} bash -c 'process_repo "$@"' _ {}
echo "ALL DONE" >> "$LOG_DIR/summary.log"

# Print summary
echo ""
echo "=== Summary ==="
for f in "$LOG_DIR"/*.log; do
  [[ "$f" == */summary.log ]] && continue
  last=$(grep -E "Merged|failed|TIMEOUT|skipping" "$f" | tail -1)
  [ -n "$last" ] && echo "$last"
done | sort

# Progress check (run separately while script is running):
#   for f in /tmp/creek_pr_logs/*.log; do
#     [[ "$f" == */summary.log ]] && continue
#     echo "--- $(basename "$f") ---"
#     grep -E "Merged|failed|Waiting|TIMEOUT|skipping|START" "$f" | tail -5
#   done
