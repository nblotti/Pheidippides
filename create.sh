curl -X POST -H "Content-Type: application/json" --data @kafa-stock-quote-connector.json http://kafka1:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @kafa-stock-quote-sinkconnector.json http://kafka1:8083/connectors
