package com.github.ecommercemall.ecommercemallsearch;

import com.alibaba.fastjson.JSON;
import com.github.ecommercemall.ecommercemallsearch.config.ElasticSearchConfig;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ECommerceMallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @ToString
    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }


    /**
     * 复杂检索:在bank中搜索address中包含mill的所有人的年龄分布以及平均年龄，平均薪资
     */
    @Test
    public void searchData() throws IOException {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();

        // 1.1）指定索引，注意这个indices是索引的意思
        searchRequest.indices("bank");

        // 1.2）构造检索条件，注意这里索引条件作为searchRequest的参数和我们的IO流很类似
        // FileInput BufferInput ObjectInput一层一层抽象，一层一层解耦
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("address", "Mill"));


        // 1.2.1)按照年龄分布进行聚合，注意TermsAggregationBuilder和terms是一一对应的
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        // 1.2.2)计算平均年龄
        AvgAggregationBuilder ageAvg = AggregationBuilders.avg("ageAvg").field("age");
        sourceBuilder.aggregation(ageAvg);

        // 1.2.3)计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        /**
         * {
         * 	"query": {
         * 		"match": {
         * 			"address": {
         * 				"query": "Mill",
         * 				"operator": "OR",
         * 				"prefix_length": 0,
         * 				"max_expansions": 50,
         * 				"fuzzy_transpositions": true,
         * 				"lenient": false,
         * 				"zero_terms_query": "NONE",
         * 				"auto_generate_synonyms_phrase_query": true,
         * 				"boost": 1.0
         *            }
         *        }
         *    },
         * 	"aggregations": {
         * 		"ageAgg": {
         * 			"terms": {
         * 				"field": "age",
         * 				"size": 10,
         * 				"min_doc_count": 1,
         * 				"shard_min_doc_count": 0,
         * 				"show_term_doc_count_error": false,
         * 				"order": [{
         * 					"_count": "desc"
         *                },                 {
         * 					"_key": "asc"
         *                }]
         *            }
         *        },
         * 		"ageAvg": {
         * 			"avg": {
         * 				"field": "age"
         *            }
         *        },
         * 		"balanceAvg": {
         * 			"avg": {
         * 				"field": "balance"
         *            }
         *        }
         *    }
         * }
         */
        System.out.println("检索条件：" + sourceBuilder);

        // 相当于是把之前请求的JSON插入进来了
        searchRequest.source(sourceBuilder);
        // 2. 执行检索
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        /**
         * {
         * 	"took": 60,
         * 	"timed_out": false,
         * 	"_shards": {
         * 		"total": 1,
         * 		"successful": 1,
         * 		"skipped": 0,
         * 		"failed": 0
         *        },
         * 	"hits": {
         * 		"total": {
         * 			"value": 4,
         * 			"relation": "eq"
         *        },
         * 		"max_score": 5.4032025,
         * 		"hits": [{
         * 			"_index": "bank",
         * 			"_type": "account",
         * 			"_id": "970",
         * 			"_score": 5.4032025,
         * 			"_source": {
         * 				"account_number": 970,
         * 				"balance": 19648,
         * 				"firstname": "Forbes",
         * 				"lastname": "Wallace",
         * 				"age": 28,
         * 				"gender": "M",
         * 				"address": "990 Mill Road",
         * 				"employer": "Pheast",
         * 				"email": "forbeswallace@pheast.com",
         * 				"city": "Lopezo",
         * 				"state": "AK"
         *            }
         *        }, {
         * 			"_index": "bank",
         * 			"_type": "account",
         * 			"_id": "136",
         * 			"_score": 5.4032025,
         * 			"_source": {
         * 				"account_number": 136,
         * 				"balance": 45801,
         * 				"firstname": "Winnie",
         * 				"lastname": "Holland",
         * 				"age": 38,
         * 				"gender": "M",
         * 				"address": "198 Mill Lane",
         * 				"employer": "Neteria",
         * 				"email": "winnieholland@neteria.com",
         * 				"city": "Urie",
         * 				"state": "IL"
         *            }
         *        }, {
         * 			"_index": "bank",
         * 			"_type": "account",
         * 			"_id": "345",
         * 			"_score": 5.4032025,
         * 			"_source": {
         * 				"account_number": 345,
         * 				"balance": 9812,
         * 				"firstname": "Parker",
         * 				"lastname": "Hines",
         * 				"age": 38,
         * 				"gender": "M",
         * 				"address": "715 Mill Avenue",
         * 				"employer": "Baluba",
         * 				"email": "parkerhines@baluba.com",
         * 				"city": "Blackgum",
         * 				"state": "KY"
         *            }
         *        }, {
         * 			"_index": "bank",
         * 			"_type": "account",
         * 			"_id": "472",
         * 			"_score": 5.4032025,
         * 			"_source": {
         * 				"account_number": 472,
         * 				"balance": 25571,
         * 				"firstname": "Lee",
         * 				"lastname": "Long",
         * 				"age": 32,
         * 				"gender": "F",
         * 				"address": "288 Mill Street",
         * 				"employer": "Comverges",
         * 				"email": "leelong@comverges.com",
         * 				"city": "Movico",
         * 				"state": "MT"
         *            }
         *        }]
         *    },
         * 	"aggregations": {
         * 		"lterms#ageAgg": {
         * 			"doc_count_error_upper_bound": 0,
         * 			"sum_other_doc_count": 0,
         * 			"buckets": [{
         * 				"key": 38,
         * 				"doc_count": 2
         *            }, {
         * 				"key": 28,
         * 				"doc_count": 1
         *            }, {
         * 				"key": 32,
         * 				"doc_count": 1
         *            }]
         *        },
         * 		"avg#ageAvg": {
         * 			"value": 34.0
         *        },
         * 		"avg#balanceAvg": {
         * 			"value": 25208.0
         *        }
         *    }
         * }
         */
        System.out.println("检索结果：" + searchResponse);

        // 3. 将检索结果封装为Bean，注意返回的不是可以直接使用的数据，我们要把原始数据处理一下
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();
            // 将JSON数据映射到一个对象中，便于我们操作
            Account account = JSON.parseObject(sourceAsString, Account.class);
            /**
             * ECommerceMallSearchApplicationTests.Account(
             * account_number=970, balance=19648,
             * firstname=Forbes, lastname=Wallace, age=28,
             * gender=M, address=990 Mill Road,employer=Pheast,
             * email=forbeswallace@pheast.com, city=Lopezo, state=AK)
             */
            System.out.println(account);
        }

        // 4. 获取聚合信息
        // 这里的操作类似于一层一层节点的剥，可以细细品味一下
        Aggregations aggregations = searchResponse.getAggregations();

        Terms ageAgg1 = aggregations.get("ageAgg");

        /**
         * "aggregations": {
         * 	"lterms#ageAgg": {
         * 		"doc_count_error_upper_bound": 0,
         * 		"sum_other_doc_count": 0,
         * 		"buckets": [{
         * 			"key": 38,
         * 			"doc_count": 2
         *                }, {
         * 			"key": 28,
         * 			"doc_count": 1
         *        }, {
         * 			"key": 32,
         * 			"doc_count": 1
         *        }]    * 	},
         * 	"avg#ageAvg": {
         * 		"value": 34.0
         *    },
         * 	"avg#balanceAvg": {
         * 		"value": 25208.0
         *    }
         * }
         */
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            // 注意这个DocCount是es默认的规则，和我们的返回字段是一致的
            System.out.println("年龄：" + keyAsString + " ==> " + bucket.getDocCount());
        }
        // 我们这里可以这样来理解
        /**
         * “aggregations”：{
         *    “ageAvg”：{
         *
         *    }
         * }
         */
        // 通过指明aggregations，来获取对应节点的数据
        Avg ageAvg1 = aggregations.get("ageAvg");
        System.out.println("平均年龄：" + ageAvg1.getValue());

        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资：" + balanceAvg1.getValue());
    }


    /**
     *
     */
    @Test
    public void searchState() throws IOException {
        //1. 创建检索请求
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //        sourceBuilder.query(QueryBuilders.termQuery("city", "Nicholson"));
        //        sourceBuilder.from(0);
        //        sourceBuilder.size(5);
        //        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("state", "AK");
        //                .fuzziness(Fuzziness.AUTO)
        //                .prefixLength(3)
        //                .maxExpansions(10);
        sourceBuilder.query(matchQueryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        searchRequest.source(sourceBuilder);
        //2. 执行检索
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse);

    }

    /**
     * 测试ES数据
     * 更新也可以
     */
    @Test
    public void indexData() throws IOException {

        // index类似于我们MySQL的插入操作
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");   //数据的id

        // indexRequest.source("userName","zhangsan","age",18,"gender","男");

        // 规范操作，直接插入一个对象
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(18);
        user.setGender("男");

        String jsonString = JSON.toJSONString(user);
        // XContentType.JSON这个玩意是官方文档写的，不是凭空出现的
        // 要保存（插入）的内容
        indexRequest.source(jsonString, XContentType.JSON);

        // 执行操作，注意client就类似于一个客户端，返回响应结果。
        // 其实和HttpClient,OkHttp,RestTemplate差不多，主要目的都是向服务器发送请求，然后获取响应的数据再做对应的处理
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        // 提取有用的响应数据
        System.out.println(index);

    }
    // 话说，nested和我们的原子性好类似啊，都是使一个东西作为最小的单元，不可分割，不可分词。
    // 简单的说，就是nested在es中使类一类的数据查询不会出现扁平化的错误

    @Getter
    @Setter
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {

        System.out.println(client);

    }
}
