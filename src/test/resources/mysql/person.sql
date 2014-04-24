DROP TABLE IF EXISTS person;

CREATE TABLE `person` (
  `id` int(11) NOT NULL,
  `name` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;