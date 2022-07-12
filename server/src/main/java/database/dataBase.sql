.open users.db

.help

.quit

.tables

.schema users

.mode line
.headers on
.mode column
.width 2 8 8 10

CREATE TABLE Users (
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 login VARCHAR (100) NOT NULL,
 password INTEGER NOT NULL,
 max_storage_size INTEGER CONSTRAINT "5555" NOT NULL DEFAULT (100000000));
 
 insert into users (login, password) 
 values ('login', 'pass');
 
 insert into users (login, password) 
 values ('login2', 'pass2');
 
 insert into users (login, password) 
 values ('login3', 'pass3');
 
 insert into users (login, password) 
 values ('login4', 'pass4');
 
 insert into users (login, password) 
 values ('login5', 'pass5');
 
 select * from users;
 
 
 
 
 
 CREATE TABLE Users (
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 login VARCHAR (100) NOT NULL,
 password INTEGER NOT NULL,
 max_storage_size INTEGER CONSTRAINT "5555" NOT NULL DEFAULT (100000000),
 server_root_directory VARCHAR (100);
 
 update users set (server_root_derectory) ="TRUE" where login = 'OK';
 
 insert into Users (login, password, server_root_derectory)
 VALUES ('OK', 10, 'OK');
 
 insert into users (login, password) 
 values ('login3', 'pass3');
 
 