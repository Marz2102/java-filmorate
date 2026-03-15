MERGE INTO genres (name) KEY(name) VALUES
       ('Комедия'),
       ('Драма'),
       ('Мультфильм'),
       ('Триллер'),
       ('Документальный'),
       ('Боевик');

MERGE INTO ratings (name) KEY(name) VALUES
       ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');