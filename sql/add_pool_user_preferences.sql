ALTER TABLE `ids`
  ADD COLUMN `pool_table_color` varchar(16) NOT NULL DEFAULT 'Classic',
  ADD COLUMN `pool_cue_tap` smallint(5) unsigned NOT NULL DEFAULT '8',
  ADD COLUMN `pool_cue_max` smallint(5) unsigned NOT NULL DEFAULT '8',
  ADD COLUMN `pool_cue_delay` smallint(5) unsigned NOT NULL DEFAULT '500',
  ADD COLUMN `pool_cue_ramp` smallint(5) unsigned NOT NULL DEFAULT '900';
