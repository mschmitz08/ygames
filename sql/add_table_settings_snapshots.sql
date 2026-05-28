ALTER TABLE `checkers_games`
  ADD COLUMN `table_settings` text AFTER `result`;

ALTER TABLE `pool_games`
  ADD COLUMN `table_settings` text AFTER `result`;

ALTER TABLE `pool_replay_games`
  ADD COLUMN `table_settings` text AFTER `result`;
