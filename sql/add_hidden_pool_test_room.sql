USE `newyahoo`;

INSERT INTO `pool_rooms` (`name`, `label`, `public`, `country`, `welcome_msg`, `id_count`)
VALUES ('test', 'Test', 0, 'us', 'Welcome to room Test', 50)
ON DUPLICATE KEY UPDATE
	`label` = VALUES(`label`),
	`public` = VALUES(`public`),
	`welcome_msg` = VALUES(`welcome_msg`);
