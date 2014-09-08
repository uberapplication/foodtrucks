(ns foodtrucks.core
  (:require [ring.util.response :refer [resource-response response]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [compojure.core :refer [GET defroutes]]
            [compojure.handler :as handler]
            [clojure.java.jdbc :as j]
            [foodtrucks.sql :refer [base-query join-query-components]]
            [foodtrucks.parameter :refer [parse-parameters]]))


; Loading the driver is apparently necessary when deploying in tomcat
(Class/forName "org.postgresql.Driver")

(def foodtrucks-db
  (let [sys-prop (System/getProperty "DB_URI")
        env-prop  (. (System/getenv) get "DB_URI")]
    {:connection-uri (or sys-prop env-prop)}))

(def food-truck-model
  "Specifies a simple table model for the food_trucks table"
  { :table "food_trucks"
    :fields [{ :name "id" }
           { :name "locationId"
             :select "location_id" }
           { :name "applicant" }
           { :name "facilityType"
             :select "facility_type" }
           { :name "status" }
           { :name "foodItems"
             :select "food_items" }
           { :name "x",
             :select "ST_X(geom)" }
           { :name "y"
             :select "ST_Y(geom)" }]}
  )

(defn reverse-geocoding-parameter
  "Applies an ORDER BY clause to the SQL query, ordering by the distance to (x,y), if
  both parameters are present."
  [components {x :x y :y}]
  (if (not-any? nil? [x y])
    (-> components
        (update-in [:order]
                   #(conj % "geom <-> ST_SetSRID(ST_MakePoint(:x, :y), 4326)"))
        (update-in [:params] #(assoc % :x x :y y)))
    components)
  )

(defn limit-parameter
  "Applies a limit clause to the SQL query if a limit parameter is present."
  [components { limit :limit }]
  (if (nil? limit)
    components
    (assoc components :limit limit)))

(def food-trucks-list-parameter-spec
  "Specifies the parameters allowed for the /foodtrucks resource"
  { :x { :type :double }
    :y { :type :double }
    :limit { :type :integer }
    :status {:type :string}
    :id { :type :integer }})

(defn filter-param
  "Return a function that applies a simple filtering parameter to
  the SQL query if present"
  [property-key]
  (fn [components params]
    (let [param-value (property-key params)]
      (if (nil? param-value)
        components
        (-> components
            (update-in [:where] #(conj % (str (name property-key) " = :" (name property-key))))
            (assoc-in [:params property-key] param-value))))))

(defn query-food-trucks [conn raw-params]
  "Queries food trucks, parsing the raw parameters from the HTTP request. The parameters will be parsed
  into the correct type, and the parameters will be combined into the appropriate SQL statement."
  (let [parsed-params (parse-parameters food-trucks-list-parameter-spec raw-params)
        [sql sqlParams] (->
                          (base-query food-truck-model)
                          (reverse-geocoding-parameter parsed-params)
                          ((filter-param :status) parsed-params)
                          ((filter-param :id) parsed-params)
                          (limit-parameter parsed-params)
                          join-query-components)]
    (println sql)
    (println sqlParams)
    (j/query
      conn
      (concat [sql] sqlParams)
      :identifiers identity))
  )

(defroutes app-routes
           (GET
             "/foodtrucks" [& params]
             (j/with-db-transaction
               [conn foodtrucks-db]
               (response (query-food-trucks conn params)))))

(def ring-handler
  (-> (handler/api app-routes)
      wrap-json-response                                    ; serve responses as JSON
      (wrap-resource "public")                              ; serve static resources
      wrap-content-type))                                   ; ensures static resources has correct content type


;(run-server app {:port 8280})