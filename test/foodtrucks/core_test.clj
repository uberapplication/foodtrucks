(ns foodtrucks.core-test
  (:require [clojure.test :refer :all]
            [foodtrucks.core :refer :all]))



(deftest reverse-geocoding-parameter-test
  (testing "reverse-geocoding-parameter"
    (testing "Given the parameters x and y, then the correct ordering clause is produced"
      (let [params {:x 1.2 :y 3.4}
            components {:params {} :order []}
            {x :x y :y} params]
        (is (= (reverse-geocoding-parameter components params)
               {:order  ["geom <-> ST_SetSRID(ST_MakePoint(:x, :y), 4326)"]
                :params {:x x :y y}}))
        ))
    (testing "Given either parameter is nil, then components is unmodified"
      (let [params {:x 1.2}
            components {:params [] :order []}]
        (is (= (reverse-geocoding-parameter components params)
               components)))
      )))

(deftest limit-parameter-test
  (testing "If a limit parameter is supplied, it will be applied to the query."
    (let [components { :limit nil }
          params { :limit 10 }
          result (limit-parameter components params)]
      (is (= result { :limit 10 })))))

(deftest filter-param-test
  (let [components {:where [] :params {}}
        filter-fn (filter-param :foo)
        params {:foo "fooValue"}
        result (filter-fn components params)]
    (testing "If a filtering parameter is supplied, the appropriate WHERE clause is added"
      (is (= result {:where ["foo = :foo"] :params {:foo "fooValue"}})))
    (testing "If no parameter is supplied, the query is unmodified"
      (is (= components (filter-fn components {}))))))