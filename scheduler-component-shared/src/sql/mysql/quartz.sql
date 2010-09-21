--
-- Quartz seems to work best with the driver mm.mysql-2.0.7-bin.jar
--

CREATE TABLE QRTZ_JOB_DETAILS
  (
    JOB_NAME  VARCHAR(80) NOT NULL,
    JOB_GROUP VARCHAR(80) NOT NULL,
    DESCRIPTION VARCHAR(120) NULL,
    JOB_CLASS_NAME   VARCHAR(128) NOT NULL,
    IS_DURABLE VARCHAR(1) NOT NULL,
    IS_VOLATILE VARCHAR(1) NOT NULL,
    IS_STATEFUL VARCHAR(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (JOB_NAME,JOB_GROUP)
);

CREATE TABLE QRTZ_JOB_LISTENERS
  (
    JOB_NAME  VARCHAR(80) NOT NULL,
    JOB_GROUP VARCHAR(80) NOT NULL,
    JOB_LISTENER VARCHAR(80) NOT NULL,
    PRIMARY KEY (JOB_NAME,JOB_GROUP,JOB_LISTENER),
    FOREIGN KEY (JOB_NAME,JOB_GROUP)
        REFERENCES QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP)
);

CREATE TABLE QRTZ_TRIGGERS
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    JOB_NAME  VARCHAR(80) NOT NULL,
    JOB_GROUP VARCHAR(80) NOT NULL,
    IS_VOLATILE VARCHAR(1) NOT NULL,
    DESCRIPTION VARCHAR(120) NULL,
    NEXT_FIRE_TIME BIGINT(13) NULL,
    PREV_FIRE_TIME BIGINT(13) NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT(13) NOT NULL,
    END_TIME BIGINT(13) NULL,
    CALENDAR_NAME VARCHAR(80) NULL,
    MISFIRE_INSTR SMALLINT(2) NULL,
    PRIORITY INT NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (JOB_NAME,JOB_GROUP)
        REFERENCES QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP)
);

CREATE TABLE QRTZ_SIMPLE_TRIGGERS
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    REPEAT_COUNT BIGINT(7) NOT NULL,
    REPEAT_INTERVAL BIGINT(12) NOT NULL,
    TIMES_TRIGGERED BIGINT(7) NOT NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_CRON_TRIGGERS
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    CRON_EXPRESSION VARCHAR(80) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_BLOB_TRIGGERS
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    BLOB_DATA BLOB NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_TRIGGER_LISTENERS
  (
    TRIGGER_NAME  VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    TRIGGER_LISTENER VARCHAR(80) NOT NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_LISTENER),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);


CREATE TABLE QRTZ_CALENDARS
  (
    CALENDAR_NAME  VARCHAR(80) NOT NULL,
    CALENDAR BLOB NOT NULL,
    PRIMARY KEY (CALENDAR_NAME)
);



CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS
  (
    TRIGGER_GROUP  VARCHAR(80) NOT NULL, 
    PRIMARY KEY (TRIGGER_GROUP)
);

CREATE TABLE QRTZ_FIRED_TRIGGERS
  (
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    IS_VOLATILE VARCHAR(1) NOT NULL,
    INSTANCE_NAME VARCHAR(80) NOT NULL,
    FIRED_TIME BIGINT(13) NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(80) NULL,
    JOB_GROUP VARCHAR(80) NULL,
    IS_STATEFUL VARCHAR(1) NULL,
    REQUESTS_RECOVERY VARCHAR(1) NULL,
    PRIORITY INT NOT NULL,
    PRIMARY KEY (ENTRY_ID)
);

CREATE TABLE QRTZ_SCHEDULER_STATE
  (
    INSTANCE_NAME VARCHAR(80) NOT NULL,
    LAST_CHECKIN_TIME BIGINT(13) NOT NULL,
    CHECKIN_INTERVAL BIGINT(13) NOT NULL,
    RECOVERER VARCHAR(80) NULL,
    PRIMARY KEY (INSTANCE_NAME)
);

CREATE TABLE QRTZ_LOCKS
  (
    LOCK_NAME  VARCHAR(40) NOT NULL, 
    PRIMARY KEY (LOCK_NAME)
);


INSERT INTO QRTZ_LOCKS values('TRIGGER_ACCESS');
INSERT INTO QRTZ_LOCKS values('JOB_ACCESS');
INSERT INTO QRTZ_LOCKS values('CALENDAR_ACCESS');
INSERT INTO QRTZ_LOCKS values('STATE_ACCESS');
INSERT INTO QRTZ_LOCKS values('MISFIRE_ACCESS');

CREATE TABLE SCHEDULER_DELAYED_INVOCATION (
	INVOCATION_ID VARCHAR(36) NOT NULL,
	INVOCATION_TIME DATETIME NOT NULL,
	COMPONENT VARCHAR(2000) NOT NULL,
	CONTEXT VARCHAR(2000) NULL,
	PRIMARY KEY (INVOCATION_ID)
);

CREATE INDEX SCHEDULER_DI_TIME_INDEX ON SCHEDULER_DELAYED_INVOCATION (INVOCATION_TIME);

INSERT INTO `QRTZ_JOB_DETAILS` VALUES ('Purge Event Logs', 'DEFAULT', null, 'org.sakaiproject.component.app.scheduler.jobs.SpringConfigurableJobBeanWrapper', '1', '0', '0', '1', 0x230a234d6f6e205365702032302031353a31383a3032204d535420323031300a6f72672e73616b616970726f6a6563742e6170692e6170702e7363686564756c65722e4a6f624265616e577261707065722e6265616e3d6576656e7450757267654a6f620a6f72672e73616b616970726f6a6563742e6170692e6170702e7363686564756c65722e4a6f624265616e577261707065722e6a6f62547970653d4576656e74204c6f672050757267650a6e756d6265722e646179733d370a);
INSERT INTO `QRTZ_TRIGGERS` VALUES ('Nightly Event Log Purge', 'DEFAULT', 'Purge Event Logs', 'DEFAULT', '0', null, '1285052400000', '-1', 'WAITING', 'CRON', '1285021140000', '0', null, '0', '5', 0x230a234d6f6e205365702032302031353a31393a3033204d535420323031300a6e756d6265722e646179733d370a);
INSERT INTO `QRTZ_CRON_TRIGGERS` VALUES ('Nightly Event Log Purge', 'DEFAULT', '0 0 0 * * ? *', 'America/Phoenix');


commit;