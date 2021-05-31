drop table IF EXISTS STOCK_FUNDAMENTAL_WEEKLY CASCADE;
CREATE TABLE STOCK_FUNDAMENTAL_WEEKLY
(
    ID                    SERIAL PRIMARY KEY,
    CODE                  varchar(50) NOT NULL,
    EXCHANGE              varchar(50) NOT NULL,
    WEEK_NUMBER           INT,
    YEAR                  INT,
    ISIN                  varchar(50),
    BOOK_VALUE            double precision,
    EBITDA                double precision,
    PE_RATIO              double precision,
    DIVIDEND_SHARE        double precision,
    DIVIDEND_YIELD        double precision,
    EARNING_SHARE         double precision,
    PROFIT_MARGIN         double precision,
    OPERTING_MARGIN_TTM   double precision,
    RETURN_ON_ASSETS_TTM  double precision,
    RETURN_ON_EQUITY_TTM  double precision,
    REVENUE_TTM           double precision,
    REVENUE_PER_SHARE_TTM double precision,
    GROSS_PROFIT_TTM      double precision,
    DILUTED_EPS_TTM       double precision,
    TRAILING_PE           double precision,
    PRICE_SALES_TTM       double precision


);

drop table IF EXISTS STOCK_FUNDAMENTAL_MONTHLY CASCADE;
CREATE TABLE STOCK_FUNDAMENTAL_MONTHLY
(
    ID                    SERIAL PRIMARY KEY,
    CODE                  varchar(50) NOT NULL,
    EXCHANGE              varchar(50) NOT NULL,
    MONTH_NUMBER          INT,
    YEAR                  INT,
    ISIN                  varchar(50),
    BOOK_VALUE            double precision,
    EBITDA                double precision,
    PE_RATIO              double precision,
    DIVIDEND_SHARE        double precision,
    DIVIDEND_YIELD        double precision,
    EARNING_SHARE         double precision,
    PROFIT_MARGIN         double precision,
    OPERTING_MARGIN_TTM   double precision,
    RETURN_ON_ASSETS_TTM  double precision,
    RETURN_ON_EQUITY_TTM  double precision,
    REVENUE_TTM           double precision,
    REVENUE_PER_SHARE_TTM double precision,
    GROSS_PROFIT_TTM      double precision,
    DILUTED_EPS_TTM       double precision,
    TRAILING_PE           double precision,
    PRICE_SALES_TTM       double precision

);