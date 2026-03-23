USE `newyahoo`;

-- Replaces the current Checkers room list with the historical Yahoo Checkers rooms
-- recovered from archived Yahoo room-directory pages and fan-maintained room lists.
DELETE FROM `checkers_rooms`;

INSERT INTO `checkers_rooms` (`name`, `label`, `public`, `country`, `welcome_msg`, `id_count`) VALUES
('amoeba_drop', 'Amoeba Drop', 1, 'us', 'Welcome to room Amoeba Drop', 50),
('badger_bridge', 'Badger Bridge', 1, 'us', 'Welcome to room Badger Bridge', 50),
('beetle_spot', 'Beetle Spot', 1, 'us', 'Welcome to room Beetle Spot', 50),
('camel_club', 'Camel Club', 1, 'us', 'Welcome to room Camel Club', 50),
('cricket_cavern', 'Cricket Cavern', 1, 'us', 'Welcome to room Cricket Cavern', 50),
('ferret_hole', 'Ferret Hole', 1, 'us', 'Welcome to room Ferret Hole', 50),
('frog_pond', 'Frog Pond', 1, 'us', 'Welcome to room Frog Pond', 50),
('hamster_pit', 'Hamster Pit', 1, 'us', 'Welcome to room Hamster Pit', 50),
('hippo_swamp', 'Hippo Swamp', 1, 'us', 'Welcome to room Hippo Swamp', 50),
('lobster_lagoon', 'Lobster Lagoon', 1, 'us', 'Welcome to room Lobster Lagoon', 50),
('mosquito_pool', 'Mosquito Pool', 1, 'us', 'Welcome to room Mosquito Pool', 50),
('owl_tree', 'Owl Tree', 1, 'us', 'Welcome to room Owl Tree', 50),
('pelican_palace', 'Pelican Palace', 1, 'us', 'Welcome to room Pelican Palace', 50),
('ram_cliff', 'Ram Cliff', 1, 'us', 'Welcome to room Ram Cliff', 50),
('seahorse_grotto', 'Seahorse Grotto', 1, 'us', 'Welcome to room Seahorse Grotto', 50),
('tarantula_theatre', 'Tarantula Theatre', 1, 'us', 'Welcome to room Tarantula Theatre', 50),
('turtle_shell', 'Turtle Shell', 1, 'us', 'Welcome to room Turtle Shell', 50),
('walrus_rock', 'Walrus Rock', 1, 'us', 'Welcome to room Walrus Rock', 50),
('weasel_way', 'Weasel Way', 1, 'us', 'Welcome to room Weasel Way', 50),
('wombat_wagon', 'Wombat Wagon', 1, 'us', 'Welcome to room Wombat Wagon', 50),
('zebra_cove', 'Zebra Cove', 1, 'us', 'Welcome to room Zebra Cove', 50),
('rec_room', 'Rec Room', 1, 'us', 'Welcome to room Rec Room', 50),
('romper_room', 'Romper Room', 1, 'us', 'Welcome to room Romper Room', 50),
('rookies_rink', 'Rookies'' Rink', 1, 'us', 'Welcome to room Rookies'' Rink', 50),
('the_boardwalk', 'The Boardwalk', 1, 'us', 'Welcome to room The Boardwalk', 50),
('tin_pan_alley', 'Tin Pan Alley', 1, 'us', 'Welcome to room Tin Pan Alley', 50),
('challenging_cafe', 'Challenging Cafe', 1, 'us', 'Welcome to room Challenging Cafe', 50),
('decent_digs', 'Decent Digs', 1, 'us', 'Welcome to room Decent Digs', 50),
('aces_orbit', 'Ace''s Orbit', 1, 'us', 'Welcome to room Ace''s Orbit', 50),
('command_central', 'Command Central', 1, 'us', 'Welcome to room Command Central', 50),
('intense_city', 'Intense City', 1, 'us', 'Welcome to room Intense City', 50)
;
