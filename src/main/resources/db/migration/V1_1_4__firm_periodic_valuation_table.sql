drop table IF EXISTS STOCK_WEEKLY_QUOTE CASCADE;
CREATE table STOCK_WEEKLY_QUOTE
(
    ID                    SERIAL PRIMARY KEY,
    EXCHANGE              varchar(50) NOT NULL,
    CODE                  varchar(50) NOT NULL,
    GIC_SECTOR            varchar(50),
    WEEK_NUMBER           int,
    YEAR                  int,
    TYPE                  varchar(50),
    MEDIAN_ADJUSTED_CLOSE double precision,
    MEDIAN_MARKET_CAP     double precision,
    MEDIAN_VOLUME         double precision,
    AVG_ADJUSTED_CLOSE    double precision,
    AVG_MARKET_CAP        double precision,
    AVG_VOLUME            double precision,
    UPDATED_DATE          date        NOT NULL
);


drop table IF EXISTS STOCK_MONTHLY_QUOTE CASCADE;
CREATE table STOCK_MONTHLY_QUOTE
(
    ID                    SERIAL PRIMARY KEY,
    EXCHANGE              varchar(50) NOT NULL,
    CODE                  varchar(50) NOT NULL,
    GIC_SECTOR            varchar(50),
    MONTH_NUMBER          int,
    YEAR                  int,
    TYPE                  varchar(50),
    MEDIAN_ADJUSTED_CLOSE double precision,
    MEDIAN_MARKET_CAP     double precision,
    MEDIAN_VOLUME         double precision,
    AVG_ADJUSTED_CLOSE    double precision,
    AVG_MARKET_CAP        double precision,
    AVG_VOLUME            double precision,
    UPDATED_DATE          date        NOT NULL
);
