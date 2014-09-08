(ns foodtrucks.sql-test
  (:require [clojure.test :refer :all]
            [foodtrucks.sql :refer :all]))

(def test-model
  { :table "TABLE"
    :fields [
              { :name "fooBar" :select "foo_bar"}
              { :name "baz" }
              ]
    })

(deftest select-clause-test
  (testing "select-clause forms correct select statement for model"
    (is (= (select-clause (:fields test-model)) "foo_bar as \"fooBar\", baz as \"baz\""))
    ))

(deftest join-query-components-test
  (testing "join-query-components"
    (testing "Will correctly joins components into a SQL string"
      (let [test-query {
                         :select "bar as foo, baz"
                         :from   "table"
                         :where  ["foo = :foo" "baz = :baz"]
                         :order  ["order1" "order2"]
                         :offset 40
                         :limit  20
                         :params { :foo "fooParam"
                                   :baz "bazParam" }
                         }
            [sql params] (join-query-components test-query)]
        (is (= sql "select bar as foo, baz from table where (foo = ?) and (baz = ?) order by order1, order2 offset ? limit ?"))
        (is (= params ["fooParam" "bazParam" 40 20]))))

    (testing "Will correctly handle omitted clauses"
      (let [test-query { :select "foo"
                         :from "table"
                         :where []
                         :order []
                         :params {}}
            [sql _] (join-query-components test-query)]
        (is (= sql "select foo from table"))))))

