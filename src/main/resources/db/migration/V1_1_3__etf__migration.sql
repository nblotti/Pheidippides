drop table IF EXISTS ETF_WEEKLY CASCADE;
CREATE TABLE ETF_WEEKLY
(
    ID                     SERIAL PRIMARY KEY,
    CODE                   varchar(50) NOT NULL,
    EXCHANGE               varchar(50) NOT NULL,
    WEEK_NUMBER            INT,
    YEAR                   INT,
    ISIN                   varchar(50),
    CURRENCY_CODE          varchar(50),
    COUNTRY_ISO            varchar(50),
    CATEGORY               varchar(50),
    YIELD                  double precision,
    NET_EXPENSE_RATIO      double precision,
    TOTAL_ASSETS           double precision,
    AVERAGE_MKT_CAP_MIL    double precision,
    BASIC_MATERIALS        double precision,
    CONSUMER_CYCLICALS     double precision,
    FINANCIAL_SERVICES     double precision,
    REAL_ESTATE            double precision,
    COMMUNICATION_SERVICES double precision,
    ENERGY                 double precision,
    INDUSTRIALS            double precision,
    TECHNOLOGY             double precision,
    CONSUMER_DEFENSIVE     double precision,
    HEALTHCARE             double precision,
    UTILITIES              double precision
);

drop table IF EXISTS ETF_MONTHLY CASCADE;
CREATE TABLE ETF_MONTHLY
(
    ID                     SERIAL PRIMARY KEY,
    CODE                   varchar(50) NOT NULL,
    EXCHANGE               varchar(50) NOT NULL,
    MONTH_NUMBER           INT,
    YEAR                   INT,
    ISIN                   varchar(50),
    CURRENCY_CODE          varchar(50),
    COUNTRY_ISO            varchar(50),
    CATEGORY               varchar(50),
    YIELD                  double precision,
    NET_EXPENSE_RATIO      double precision,
    TOTAL_ASSETS           double precision,
    AVERAGE_MKT_CAP_MIL    double precision,
    BASIC_MATERIALS        double precision,
    CONSUMER_CYCLICALS     double precision,
    FINANCIAL_SERVICES     double precision,
    REAL_ESTATE            double precision,
    COMMUNICATION_SERVICES double precision,
    ENERGY                 double precision,
    INDUSTRIALS            double precision,
    TECHNOLOGY             double precision,
    CONSUMER_DEFENSIVE     double precision,
    HEALTHCARE             double precision,
    UTILITIES              double precision
);

