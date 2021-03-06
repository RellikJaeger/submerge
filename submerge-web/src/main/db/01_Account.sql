
CREATE TABLE public.ACCOUNT_STATUS (
                ID INTEGER NOT NULL,
                INFO VARCHAR(10) NOT NULL,
                CONSTRAINT pk_account_status PRIMARY KEY (ID)
);
COMMENT ON TABLE public.ACCOUNT_STATUS IS 'Status of the user account(i.e. enabled / disabled)';
COMMENT ON COLUMN public.ACCOUNT_STATUS.ID IS 'User account status id';
COMMENT ON COLUMN public.ACCOUNT_STATUS.INFO IS 'Short description of the status';


CREATE SEQUENCE public.s_account_id;

CREATE TABLE public.ACCOUNT (
                ID INTEGER NOT NULL DEFAULT nextval('public.s_account_id'),
                NAME VARCHAR(20) NOT NULL,
                PASSWORD CHAR(64) NOT NULL,
                EMAIL VARCHAR(320) NOT NULL,
                CREATION TIMESTAMP NOT NULL,
                FK_ACCOUNT_STATUS INTEGER NOT NULL,
                LAST_LOGIN TIMESTAMP NOT NULL,
                LAST_UPDATE TIMESTAMP NOT NULL,
                CONSTRAINT pk_account PRIMARY KEY (ID)
);
COMMENT ON COLUMN public.ACCOUNT.ID IS 'Primary key';
COMMENT ON COLUMN public.ACCOUNT.NAME IS 'Username';
COMMENT ON COLUMN public.ACCOUNT.PASSWORD IS 'sha256 password';
COMMENT ON COLUMN public.ACCOUNT.EMAIL IS 'Email address';
COMMENT ON COLUMN public.ACCOUNT.CREATION IS 'Date Account Was Created';
COMMENT ON COLUMN public.ACCOUNT.FK_ACCOUNT_STATUS IS 'User account status id';
COMMENT ON COLUMN public.ACCOUNT.LAST_LOGIN IS 'Las time the user ha been logged in';
COMMENT ON COLUMN public.ACCOUNT.LAST_UPDATE IS 'Last time the row has been updated';


ALTER SEQUENCE public.s_account_id OWNED BY public.ACCOUNT.ID;

ALTER TABLE public.ACCOUNT ADD CONSTRAINT account_status_user_fk
FOREIGN KEY (FK_ACCOUNT_STATUS)
REFERENCES public.ACCOUNT_STATUS (ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;


ALTER TABLE account ADD CONSTRAINT cons_account_email UNIQUE (email);
ALTER TABLE account ADD CONSTRAINT cons_account_name UNIQUE (name);

insert into account_status values('1', 'ENABLED');
insert into account_status values('2', 'DISABLED');
insert into account_status values('3', 'LOCKED');



CREATE TABLE PERSISTENT_LOGINS (
                SERIES VARCHAR(64) NOT NULL,
                FK_ID_ACCOUNT INTEGER NOT NULL,
                TOKEN VARCHAR(64) NOT NULL,
                LAST_USED TIMESTAMP NOT NULL,
                CONSTRAINT PK_PERSISTENT_LOGINS PRIMARY KEY (SERIES)
);
COMMENT ON TABLE PERSISTENT_LOGINS IS 'Store logins using remember me service';
COMMENT ON COLUMN PERSISTENT_LOGINS.SERIES IS 'Primary key ';
COMMENT ON COLUMN PERSISTENT_LOGINS.FK_ID_ACCOUNT IS 'Account foreign key';
COMMENT ON COLUMN PERSISTENT_LOGINS.TOKEN IS 'Token';
COMMENT ON COLUMN PERSISTENT_LOGINS.LAST_USED IS 'Last time the token has been used';

ALTER TABLE PERSISTENT_LOGINS ADD CONSTRAINT FK_USER_PERSISTENT_LOGINS
FOREIGN KEY (FK_ID_ACCOUNT)
REFERENCES ACCOUNT (ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;


ALTER TABLE PERSISTENT_LOGINS ADD CONSTRAINT cons_PERSISTENT_LOGINS UNIQUE (TOKEN);



CREATE TABLE USER_AUTHORITIES (
                FK_ID_ACCOUNT INTEGER NOT NULL,
                FK_ID_AUTHORITIES INTEGER NOT NULL,
                LAST_UPDATE TIMESTAMP NOT NULL,
                CONSTRAINT pk_user_authorities PRIMARY KEY (FK_ID_ACCOUNT, FK_ID_AUTHORITIES)
);
COMMENT ON TABLE USER_AUTHORITIES IS 'Spring roles';
COMMENT ON COLUMN USER_AUTHORITIES.FK_ID_ACCOUNT IS 'Account Foreign key';
COMMENT ON COLUMN USER_AUTHORITIES.FK_ID_AUTHORITIES IS 'Spring roles';
COMMENT ON COLUMN USER_AUTHORITIES.LAST_UPDATE IS 'Last time the row has been updated';


CREATE SEQUENCE s_authorities_id;

CREATE TABLE AUTHORITIES (
                ID INTEGER NOT NULL DEFAULT nextval('s_authorities_id'),
                NAME VARCHAR(10) NOT NULL,
                CONSTRAINT pk_authorities PRIMARY KEY (ID)
);
COMMENT ON TABLE AUTHORITIES IS 'Primary key';
COMMENT ON COLUMN AUTHORITIES.ID IS 'Spring roles';
COMMENT ON COLUMN AUTHORITIES.NAME IS 'Description';



ALTER TABLE USER_AUTHORITIES ADD CONSTRAINT authorities_user_authorities_fk
FOREIGN KEY (FK_ID_AUTHORITIES)
REFERENCES AUTHORITIES (ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE USER_AUTHORITIES ADD CONSTRAINT account_authorities_fk
FOREIGN KEY (FK_ID_ACCOUNT)
REFERENCES ACCOUNT (ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;


insert into authorities(id,name) values('2', 'ROLE_USER');
insert into authorities(id,name) values('1', 'ROLE_ADMIN');

alter table SUBTITLE_PROFILE add LAST_UPDATE TIMESTAMP NOT NULL;
alter table DUAL_SUBTITLE_CONFIG add LAST_UPDATE TIMESTAMP NOT NULL;

alter table account alter column email drop not null;