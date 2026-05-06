CREATE TABLE IF NOT EXISTS `id_avatars` (
  `name` varchar(32) NOT NULL DEFAULT '',
  `mime` varchar(32) NOT NULL DEFAULT 'image/png',
  `image` mediumblob NOT NULL,
  `version` int(10) unsigned NOT NULL DEFAULT '1',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
