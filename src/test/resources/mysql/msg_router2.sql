DROP TABLE IF EXISTS msg_3;
DROP TABLE IF EXISTS msg_4;
DROP TABLE IF EXISTS msg_5;

CREATE TABLE `msg_3` (
  `id` bigint(21) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `content` varchar(25) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `msg_4` (
  `id` bigint(21) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `content` varchar(25) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `msg_5` (
  `id` bigint(21) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `content` varchar(25) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
