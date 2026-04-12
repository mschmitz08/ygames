USE `newyahoo`;

-- Keep all historical pool rooms in the table, but only surface the curated
-- set in the launcher dropdowns and room browsers.
UPDATE `pool_rooms` SET `public` = 0;

UPDATE `pool_rooms` SET `public` = 1
WHERE `name` IN (
    'advanced_lounge',
    'beginner_lounge',
    'intermediate_lounge',
    'social_lounge',
    '8_ball_nightmare',
    'fast_eddies',
    'high_rollers',
    'low_rollers',
    'minnesota_fats',
    'players_choice',
    'pool_hustlers',
    'pool_sharks',
    'rack_it_up',
    'side_pocket',
    'trick_shot'
);
