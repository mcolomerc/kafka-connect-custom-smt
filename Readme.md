
# Kafka Connect Transformer

## Overview 

Example of a configuration for a `RegExpTransform` transformation.

```json
{
"transforms": "RegExpTransform",
"transforms.RegExpTransform.type": "org.mcolomerc.RegexpFieldTransformer",
"transforms.RegExpTransform.field": "username",
"transforms.RegExpTransform.regexp": "(\\w{3})\\w+",
"transforms.RegExpTransform.replacement": "$1***"
}
```

Input record:

```json
{
  "username": "johndoe123",
  "email": "johndoe@example.com"
}
```

Output record:

```json
{
  "username": "joh***", 
  "email": "johndoe@example.com"
}
```


## Datagen Example 

```json
{
  "name": "DatagenSourceConnector_0",
  "config": {
    "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
    "name": "DatagenSourceConnector_0",
    "kafka.topic": "orders",
    "output.data.format": "JSON",
    "quickstart": "ORDERS",
    "tasks.max": "1",
    "transforms": "RegExpTransform",
    "transforms.RegExpTransform.type": "org.mcolomerc.RegexpFieldTransformer",
    "transforms.RegExpTransform.field": "itemid",
    "transforms.RegExpTransform.regexp": "(Item_)(\\d+)",
    "transforms.RegExpTransform.replacement": "It_$2_em"
  }
}
```

```json
{
"ordertime": 1515148008825,
"orderid": 197,
"itemid": "It_747_em",
"orderunits": 7.513786715933578,
"address": {
"city": "City_35",
"state": "State_",
"zipcode": 59774
}
}
```

Convert FIRST_NAME (Alice) to --> -A-l-i-c-e-

```json
    "transforms": "RegExpTransform",
    "transforms.RegExpTransform.type": "org.mcolomerc.RegexpFieldTransformer",
    "transforms.RegExpTransform.field": "FIRST_NAME",
    "transforms.RegExpTransform.regexp": "([A-Za-z])",
    "transforms.RegExpTransform.replacement": "-$1-" 
```

## Package 

```sh
mvn clean package
```

## Deploy

Connect Plugin path: 

```sh
export PLUGIN_PATH=/usr/share/java
```

```sh
cp target/kafka-connect-transformer-1.0-SNAPSHOT-jar-with-dependencies.jar $PLUGIN_PATH
```

 
