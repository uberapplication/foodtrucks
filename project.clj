(defproject food-truck-finder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-exec "0.3.4"]
            [lein-bower "0.5.1"]
            [lein-ring "0.8.11"]
            [lein-beanstalk "0.2.7"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [compojure "1.1.8"]
                 [ring/ring-json "0.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41" ]
                 [org.clojure/data.csv "0.1.2"]
                 [csv-map "0.1.0"]]
  :bower-dependencies [[leaflet "0.7.3"]
                       [jquery "2.1.1"]
                       [bootstrap "3.2.0"]]
  :ring {:handler foodtrucks.core/ring-handler}
  :aws {:beanstalk { :region "eu-west-1"
                     :environments [{:name "food-truck-finder-prod"
                                    :cname-prefix "food-truck-finder"}]}})
