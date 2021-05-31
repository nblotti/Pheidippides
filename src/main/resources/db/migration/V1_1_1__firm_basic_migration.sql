drop table IF EXISTS STOCK_BASIC_WEEKLY CASCADE;
CREATE TABLE STOCK_BASIC_WEEKLY
(
    ID                        SERIAL PRIMARY KEY,
    CODE                      varchar(50) NOT NULL,
    EXCHANGE                  varchar(50) NOT NULL,
    WEEK_NUMBER               INT,
    YEAR                      INT,
    ISIN                      varchar(50),
    CURRENCY_CODE             varchar(50),
    COUNTRY_ISO               varchar(50),

    GIC_SECTOR                varchar(50),
    GIC_GROUP                 varchar(50),
    GIC_INDUSTRY              varchar(100),
    GIC_SUB_INDUSTRY          varchar(100),
    MEDIAN_MARKET_CAP         double precision,
    MEDIAN_ADJUSTED_CLOSE     double precision,
    MEDIAN_VOLUME             double precision,
    AVG_MARKET_CAP            double precision,
    AVG_ADJUSTED_CLOSE        double precision,
    AVG_VOLUME                double precision,
    SHARES_OUTSTANDING        bigint,
    SHARES_FLOAT              bigint,
    PERCENT_INSIDERS          double precision,
    PERCENT_INSTITUTIONS      double precision,
    SHARES_SHORT              bigint,
    SHORT_RATIO               double precision,
    SHORT_PERCENT_OUTSTANDING double precision,
    SHORT_PERCENT_FLOAT       double precision


);

drop table IF EXISTS STOCK_BASIC_MONTHLY CASCADE;
CREATE TABLE STOCK_BASIC_MONTHLY
(
    ID                        SERIAL PRIMARY KEY,
    CODE                      varchar(50) NOT NULL,
    EXCHANGE                  varchar(50) NOT NULL,
    MONTH_NUMBER              INT,
    YEAR                      INT,
    ISIN                      varchar(50),
    CURRENCY_CODE             varchar(50),
    COUNTRY_ISO               varchar(50),
    GIC_SECTOR                varchar(50),
    GIC_GROUP                 varchar(50),
    GIC_INDUSTRY              varchar(100),
    GIC_SUB_INDUSTRY          varchar(100),
    MEDIAN_MARKET_CAP         double precision,
    MEDIAN_ADJUSTED_CLOSE     double precision,
    MEDIAN_VOLUME             double precision,
    AVG_MARKET_CAP            double precision,
    AVG_ADJUSTED_CLOSE        double precision,
    AVG_VOLUME                double precision,
    SHARES_OUTSTANDING        bigint,
    SHARES_FLOAT              bigint,
    PERCENT_INSIDERS          double precision,
    PERCENT_INSTITUTIONS      double precision,
    SHARES_SHORT              bigint,
    SHORT_RATIO               double precision,
    SHORT_PERCENT_OUTSTANDING double precision,
    SHORT_PERCENT_FLOAT       double precision
);
