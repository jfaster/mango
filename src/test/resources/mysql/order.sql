DROP TABLE IF EXISTS t_order;

CREATE TABLE `t_order` (
  `id` varchar(25) NOT NULL,
  `uid` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
