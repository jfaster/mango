DROP TABLE IF EXISTS byte_info;

CREATE TABLE `byte_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `array_byte` varbinary(1000) NOT NULL,
  `single_byte` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;