
create table categories
(
    id   bigint auto_increment
        primary key,
    name varchar(255) null
);

create table institutions
(
    id          bigint auto_increment
        primary key,
    description varchar(255) null,
    name        varchar(255) null
);

create table user_types
(
    id   bigint auto_increment
        primary key,
    role varchar(255) null
);

create table users
(
    blocked                bit          null,
    is_active              bit          null,
    id                     bigint auto_increment
        primary key,
    registration_date_time datetime(6)  null,
    email                  varchar(255) null,
    password               varchar(255) null
);

create table user_profiles
(
    id            bigint auto_increment
        primary key,
    user_id       bigint       null,
    profile_photo varchar(64)  null,
    city          varchar(255) null,
    country       varchar(255) null,
    first_name    varchar(255) null,
    last_name     varchar(255) null,
    phone_number  varchar(255) null,
    constraint UKe5h89rk3ijvdmaiig4srogdc6
        unique (user_id),
    constraint FKjcad5nfve11khsnpwj1mv8frj
        foreign key (user_id) references users (id)
);

create table donations
(
    pick_up_date         date         null,
    pick_up_time         time(6)      null,
    quantity             int          null,
    received             bit          not null,
    created              datetime(6)  null,
    donation_passed_time datetime(6)  null,
    id                   bigint auto_increment
        primary key,
    institution_id       bigint       null,
    user_id              bigint       null,
    city                 varchar(255) null,
    phone_number         varchar(255) null,
    pick_up_comment      varchar(255) null,
    street               varchar(255) null,
    zip_code             varchar(255) null,
    constraint FK4vbbe785haqnjerv22xjehvbg
        foreign key (institution_id) references institutions (id),
    constraint FKd2p196clbvqgbemy05ndspwu
        foreign key (user_id) references users (id)
);

create table donations_categories
(
    category_id bigint not null,
    donation_id bigint not null,
    constraint FK8vte7y1uahh9hvijwuyp0teci
        foreign key (donation_id) references donations (id),
    constraint FKrp6h7l1e7u6lt393c7xrlw688
        foreign key (category_id) references categories (id)
);

create table password_reset_verification_tokens
(
    expiration_time datetime(6)  null,
    id              bigint auto_increment
        primary key,
    user_id         bigint       null,
    token           varchar(255) null,
    consumed             bit          not null,
    constraint UK2gcssaiw9c26pm5db9cg9d5j8
        unique (user_id),
    constraint FKa6npxhrd9psite9f4ynvx0ukm
        foreign key (user_id) references users (id)
);

create table users_user_types
(
    user_id      bigint not null,
    user_type_id bigint not null,
    constraint FKg513rbs7xa0jo9k961vmkwvut
        foreign key (user_id) references users (id),
    constraint FKgrdf2sulieulu3jwtb7qkej28
        foreign key (user_type_id) references user_types (id)
);

create table verification_tokens
(
    expiration_time datetime(6)  null,
    id              bigint auto_increment
        primary key,
    user_id         bigint       null,
    token           varchar(255) null,
    constraint UKdqp95ggn6gvm865km5muba2o5
        unique (user_id),
    constraint FK54y8mqsnq1rtyf581sfmrbp4f
        foreign key (user_id) references users (id)
);

INSERT INTO `user_types` VALUES (1,'ROLE_USER');
INSERT INTO `user_types` VALUES (2,'ROLE_ADMIN');



