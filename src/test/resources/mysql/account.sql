DROP TABLE IF EXISTS account;

CREATE TABLE `account` (
  `id` int(11) NOT NULL,
  `balance` int(11),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;