CREATE TABLE IF NOT EXISTS `pool_replay_reports` (
  `report_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `replay_key` char(36) NOT NULL,
  `report_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reporter` varchar(32) DEFAULT NULL,
  `room_name` varchar(32) NOT NULL DEFAULT '',
  `table_number` smallint(5) unsigned NOT NULL,
  `event_seq` int(10) unsigned DEFAULT NULL,
  `comment` varchar(500) NOT NULL,
  PRIMARY KEY (`report_id`),
  KEY `pool_replay_reports_replay_time` (`replay_key`,`report_time`),
  CONSTRAINT `pool_replay_reports_game_fk` FOREIGN KEY (`replay_key`) REFERENCES `pool_replay_games` (`replay_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `pool_replay_reports`
  MODIFY `comment` varchar(500) NOT NULL;
