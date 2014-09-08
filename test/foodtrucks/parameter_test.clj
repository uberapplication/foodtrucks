(ns foodtrucks.parameter-test
  (:require [clojure.test :refer :all]
            [foodtrucks.parameter :refer :all]))

(deftest parse-parameter-test
         (testing "Can parse a double parameter"
                  (is (= 1.2 (parse-parameter {:type :double} "1.2"))))
         (testing "Can parse a string parameter"
                  (is (= "foo bar" (parse-parameter {:type :string} "foo bar"))))
         (testing "Can parse an integer parameter"
                  (is (= 23 (parse-parameter {:type :integer} "23")))))

(deftest parse-parameters-test
         (testing "Will parse a map of raw parameters correctly",
                  (let [raw-params {:a "1.2" :b "3" :c "test"}
                        parameter-specs { :a { :type :double } :b { :type :integer }}
                        result (parse-parameters parameter-specs raw-params)]
                    (testing "The parameters are parsed into the right type"
                             (is (= 1.2 (:a result)))
                             (is (= 3 (:b result))))
                    (testing "Non-specified parameters are removed"
                             (is (nil? (:c result)))))))