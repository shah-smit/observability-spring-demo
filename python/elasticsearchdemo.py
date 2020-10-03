from datetime import datetime,timedelta
from elasticsearch import Elasticsearch


def get_file_name():
    return datetime.today().strftime('%Y-%m-%d')


def get_query_start_range():
    # Yesterday 3.30(UTC)
    yesterday = datetime.now() - timedelta(1)
    updatedDate = datetime.strftime(yesterday, '%Y-%m-%d')
    return updatedDate+"T15:30:00"


def get_query_end_range():
    # Today 3.30(UTC)
    today = datetime.today().strftime('%Y-%m-%d')
    return today+"T15:30:00"


def get_query_json():
    return {
        "_source": ["wbsp","@timestamp"],
        "query": {
            "range": {
                "@timestamp": {
                    "gte": get_query_start_range(),
                    "lt": get_query_end_range()
                }
            }
        }
    }


es = Elasticsearch(["http://localhost:9005"])

# doc = {
#     'author': 'Shah Smit',
#     'text': 'Elasticsearch: cool. bonsai cool.',
#     'timestamp': datetime.now(),
# }

index_toSearch = "business_logstash_wbsp"

# res = es.index(index=index_toSearch, id=1, body=doc)
# print(res['result'])
#
# res = es.get(index=index_toSearch, id=1)
# print(res['_source'])

es.indices.refresh(index=index_toSearch)

res = es.search(index=index_toSearch, body=get_query_json())
print("Got %d Hits:" % res['hits']['total']['value'])

hs = open(get_file_name(), "a")
for hit in res['hits']['hits']:
    hs.write(hit['_source']['wbsp'])
    hs.write("\n")
hs.close()
