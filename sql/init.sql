CREATE TABLE
    voctech1;

USE voctech1;

SET
    FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS themes_relations,
words_relations,
words,
themes,
users;

SET
    FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `themes`;

CREATE TABLE
    `themes` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `name_fr` varchar(255) CHARACTER
        SET
            utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
            `name_en` varchar(255) NOT NULL,
            `desc_fr` text NOT NULL,
            `desc_en` text NOT NULL,
            PRIMARY KEY (`id`)
    ) ENGINE = InnoDB AUTO_INCREMENT = 7 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `users`;

CREATE TABLE
    `users` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `email` varchar(255) NOT NULL,
        `password` varchar(255) NOT NULL,
        `pseudo` varchar(100) NOT NULL,
        `ranking` int DEFAULT '0',
        PRIMARY KEY (`id`),
        UNIQUE KEY `email` (`email`),
        UNIQUE KEY `pseudo` (`pseudo`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `words`;

CREATE TABLE
    `words` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `language` char(3) NOT NULL,
        `word` varchar(255) NOT NULL,
        PRIMARY KEY (`id`)
    ) ENGINE = InnoDB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `themes_relations`;

CREATE TABLE
    `themes_relations` (
        `word_id` bigint NOT NULL,
        `theme_id` bigint NOT NULL,
        PRIMARY KEY (`word_id`, `theme_id`),
        KEY `theme_id` (`theme_id`),
        CONSTRAINT `themes_relations_ibfk_1` FOREIGN KEY (`word_id`) REFERENCES `words` (`id`) ON DELETE CASCADE,
        CONSTRAINT `themes_relations_ibfk_2` FOREIGN KEY (`theme_id`) REFERENCES `themes` (`id`) ON DELETE CASCADE
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `words_relations`;

CREATE TABLE
    `words_relations` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `word_source_id` bigint NOT NULL,
        `word_target_id` bigint NOT NULL,
        `type` enum ('translation', 'synonym', 'antonym') NOT NULL DEFAULT 'translation',
        PRIMARY KEY (`id`),
        UNIQUE KEY `unique_relation` (`word_source_id`, `word_target_id`, `type`),
        KEY `word_target_id` (`word_target_id`),
        CONSTRAINT `words_relations_ibfk_1` FOREIGN KEY (`word_source_id`) REFERENCES `words` (`id`) ON DELETE CASCADE,
        CONSTRAINT `words_relations_ibfk_2` FOREIGN KEY (`word_target_id`) REFERENCES `words` (`id`) ON DELETE CASCADE
    ) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO
    `themes`
VALUES
    (
        1,
        'Méthodologie',
        'Methodology',
        'Cette rubrique regroupe les informations concernant les aspects liés à la fouille et l\'étude du mobilier. Au cours de cette partie méthodologique sont également traités les termes en lien avec la dimension chronologique de toute étude archéologique ainsi que le vocabulaire utilisé dans la description des sociétés du passé.',
        'This section gathers information related to excavation and the study of artifacts. This methodological part also covers terms related to the chronological dimension of any archaeological study, as well as the vocabulary used in the description of past societies.'
    ),
    (
        2,
        'Production',
        'Production',
        'Cette thématique a pour objectif de traiter des productions matérielles humaines. Elle regroupe donc les objets produits durant l\'époque considérée dans cet ouvrage ainsi que les techniques mises en œuvre. On y trouvera les éléments concernant les productions artistiques. Cette rubrique prend également en compte les actions humaines liées à la production agricole ainsi que celles liées au transport.',
        'This theme aims to address human material productions. It therefore includes objects produced during the period covered in this work, as well as the techniques used. It also encompasses elements related to artistic productions. This section also considers human activities related to agricultural production as well as those linked to transportation.'
    ),
    (
        3,
        'Funéraire',
        'Funerary',
        'Dans cette partie de l\'ouvrage est pris en considération l\'ensemble des éléments liés à l\'archéologie funéraire autant dans la description des rites funéraires que dans celle de l\'anthropologie physique. Cette thématique recouvre également les aspects liés aux manifestations culturelles.',
        'This section of the work considers all elements related to funerary archaeology, both in the description of funeral rites and in physical anthropology. This theme also encompasses aspects related to cultural manifestations.'
    ),
    (
        4,
        'Guerre et chasse',
        'War and Hunting',
        'La thématique abordée tend à prendre en considération le vocabulaire nécessaire à l\'étude des pratiques de guerre et de prédation, à savoir la chasse et la pêche. L\'approche du vocabulaire lié à la guerre doit prendre en considération les outils (les armes) mais également les structures associées comme les éléments de fortification.',
        'This theme aims to encompass the vocabulary necessary for the study of war and predation practices, namely hunting and fishing. The approach to war-related vocabulary must take into account not only tools (weapons) but also associated structures, such as fortification elements.'
    ),
    (
        5,
        'Habitat',
        'Habitat',
        'Cette partie recouvre le vocabulaire lié aux différents types d\'habitats mais également des éléments plus précis concernant l\'architecture et la construction des bâtiments.',
        'This section covers the vocabulary related to different types of habitats, as well as more specific elements concerning architecture and building construction.'
    ),
    (
        6,
        'Environnement',
        'Environment',
        'Nous avons souhaité souligner la place importante de l\'environnement dans toute étude archéologique en mettant en évidence le vocabulaire lié aux informations climatiques, géologiques ainsi qu\'à la faune et la flore.',
        'We wanted to emphasize the important role of the environment in any archaeological study by highlighting the vocabulary related to climatic and geological information, as well as fauna and flora.'
    );