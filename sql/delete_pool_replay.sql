-- Set the replay key you want to remove, then run this script.
-- Deleting from the parent table cascades to pool_replay_events.
SET @replay_key = 'replace-with-replay-key';

START TRANSACTION;
DELETE FROM `pool_replay_games`
WHERE `replay_key` = @replay_key;
COMMIT;
