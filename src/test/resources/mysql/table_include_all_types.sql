DROP TABLE IF EXISTS `table_include_all_types`;

CREATE TABLE `table_include_all_types` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nav_byte` int(11) DEFAULT NULL,
  `nav_short` int(11) DEFAULT NULL,
  `nav_integer` int(11) DEFAULT NULL,
  `nav_long` bigint(21) DEFAULT NULL,
  `nav_float` float(11,2) DEFAULT NULL,
  `nav_double` double(11,2) DEFAULT NULL,
  `nav_bollean` tinyint(11) DEFAULT NULL,
  `nav_char` char(50) DEFAULT NULL,
  `obj_byte` int(11) DEFAULT NULL,
  `obj_short` int(11) DEFAULT NULL,
  `obj_integer` int(11) DEFAULT NULL,
  `obj_long` bigint(21) DEFAULT NULL,
  `obj_float` float DEFAULT NULL,
  `obj_double` double DEFAULT NULL,
  `obj_bollean` tinyint(4) DEFAULT NULL,
  `obj_char` char(50) DEFAULT NULL,
  `obj_string` varchar(100) DEFAULT NULL,
  `obj_big_decimal` decimal(50,2) DEFAULT NULL,
  `obj_big_integer` decimal(50,0) DEFAULT NULL,
  `nav_bytes` varbinary(100) DEFAULT NULL,
  `obj_bytes` varbinary(100) DEFAULT NULL,
  `obj_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;