/*! SET storage_engine=INNODB */;

drop table if exists qualpay_responses;
create table qualpay_responses (
  record_id serial
, kb_account_id char(36) not null
, kb_payment_id char(36) not null
, kb_payment_transaction_id char(36) not null
, transaction_type varchar(32) not null
, amount numeric(15,9)
, currency char(3)
, qualpay_id varchar(255) not null
, additional_data longtext default null
, created_date datetime not null
, kb_tenant_id char(36) not null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create index qualpay_responses_kb_payment_id on qualpay_responses(kb_payment_id);
create index qualpay_responses_kb_payment_transaction_id on qualpay_responses(kb_payment_transaction_id);
create index qualpay_responses_qualpay_id on qualpay_responses(qualpay_id);

drop table if exists qualpay_payment_methods;
create table qualpay_payment_methods (
  record_id serial
, kb_account_id char(36) not null
, kb_payment_method_id char(36) not null
, qualpay_id varchar(255) not null
, is_deleted smallint not null default 0
, additional_data longtext default null
, created_date datetime not null
, updated_date datetime not null
, kb_tenant_id char(36) not null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create unique index qualpay_payment_methods_kb_payment_id on qualpay_payment_methods(kb_payment_method_id);
create index qualpay_payment_methods_qualpay_id on qualpay_payment_methods(qualpay_id);

