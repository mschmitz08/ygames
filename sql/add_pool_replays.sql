CREATE TABLE IF NOT EXISTS `pool_replay_games` (
  `replay_key` char(36) NOT NULL,
  `room_name` varchar(32) NOT NULL DEFAULT '',
  `table_number` smallint(5) unsigned NOT NULL,
  `game_type` smallint(5) unsigned NOT NULL,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ended_at` timestamp NULL DEFAULT NULL,
  `player0` varchar(32) DEFAULT NULL,
  `player1` varchar(32) DEFAULT NULL,
  `flags` bigint(20) unsigned DEFAULT NULL,
  `initial_state` mediumblob,
  `stop_data` blob,
  `result` blob,
  PRIMARY KEY (`replay_key`),
  KEY `pool_replay_games_started_at` (`started_at`),
  KEY `pool_replay_games_room_table` (`room_name`,`table_number`,`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `pool_replay_events` (
  `replay_key` char(36) NOT NULL,
  `seq` int(10) unsigned NOT NULL,
  `event_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actor_seat` tinyint(3) NOT NULL DEFAULT '-1',
  `event_type` varchar(24) NOT NULL,
  `payload` blob,
  PRIMARY KEY (`replay_key`,`seq`),
  KEY `pool_replay_events_type` (`event_type`),
  CONSTRAINT `pool_replay_events_game_fk` FOREIGN KEY (`replay_key`) REFERENCES `pool_replay_games` (`replay_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
