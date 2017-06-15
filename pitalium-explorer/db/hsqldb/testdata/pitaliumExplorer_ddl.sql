DROP TABLE IF EXISTS similarity;
DROP TABLE IF EXISTS methods;
DROP TABLE IF EXISTS shifts;
DROP TABLE IF EXISTS rectangles;
DROP TABLE IF EXISTS images_pairs;
DROP TABLE IF EXISTS images;
DROP TABLE IF EXISTS browsers;
DROP TABLE IF EXISTS urls;


CREATE TABLE urls (
	id INTEGER IDENTITY,
	url VARCHAR(255)
);

CREATE TABLE browsers(
	id INTEGER IDENTITY,
	name VARCHAR(255)
);

CREATE TABLE images(
	id INTEGER IDENTITY,
	browser_id INTEGER,
	url_id INTEGER,
	path VARCHAR(255) NOT NULL,
	FOREIGN KEY (browser_id) REFERENCES browsers(id),
	FOREIGN KEY (url_id) REFERENCES urls(id)
);

CREATE TABLE images_pairs(
	id INTEGER IDENTITY,
	img1_id INTEGER,
	img2_id INTEGER,
	FOREIGN KEY(img1_id) REFERENCES images(id),
	FOREIGN KEY(img2_id) REFERENCES images(id)
);

CREATE TABLE rectangles(
	id INTEGER IDENTITY,
	images_pair_id INTEGER,
	x DECIMAL(5,1) NOT NULL,
	y DECIMAL(5,1) NOT NULL,
	width DECIMAL(5,1) NOT NULL,
	height DECIMAL(5,1) NOT NULL
);

CREATE TABLE shifts(
	rectangle_id INTEGER,
	shifted_x DECIMAL(5,1) NOT NULL,
	shifted_y DECIMAL(5,1) NOT NULL,
	PRIMARY KEY (rectangle_id),
	FOREIGN KEY (rectangle_id) REFERENCES rectangles(id)
);

CREATE TABLE methods(
	id INTEGER IDENTITY,
	name VARCHAR(255)
);

CREATE TABLE similarity(
	rectangle_id INTEGER,
	method_id INTEGER,
	similarity DECIMAL(5,1) NOT NULL,
	shifted_x DECIMAL(5,1) NOT NULL,
	shifted_y DECIMAL(5,1) NOT NULL,
	PRIMARY KEY(rectangle_id, method_id),
	FOREIGN KEY(rectangle_id) REFERENCES rectangles(id),
	FOREIGN KEY(method_id) REFERENCES methods(id)
);