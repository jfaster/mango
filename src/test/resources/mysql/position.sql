DROP TABLE IF EXISTS `pos`;

CREATE TABLE `pos` (
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `v` int(11) NOT NULL,
  PRIMARY KEY (`x`,`y`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;