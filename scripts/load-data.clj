; This script loads data into the foodtrucks database

(require '[clojure.java.jdbc :as j]
         '[clojure.java.io :as io]
         '[csv-map.core :as csv]
         '[clojure.string :refer [blank?]])

(println (System/getenv))

(def foodtrucks-db {:connection-uri (. (System/getenv) get "DB_URI")})

(defn parseDoubleOrBlank [str]
  (if (blank? str)
    nil
    (. Double parseDouble str)))

(with-open [reader (io/reader (second *command-line-args*))]
  (j/with-db-transaction
    [conn foodtrucks-db]
    (doseq [csv-line (csv/parse-csv reader)]
      (println csv-line)
      (j/execute!
        conn
        ["insert into food_trucks(location_id, applicant, facility_type, status, food_items, geom)
         values (?,?,?,?,?,ST_SetSRID(ST_MakePoint(?,?),4326))"
         (Integer/parseInt (get csv-line "locationid"))
         (get csv-line "Applicant")
         (get csv-line "FacilityType")
         (get csv-line "Status")
         (get csv-line "FoodItems")
         (parseDoubleOrBlank (get csv-line "Longitude"))
         (parseDoubleOrBlank (get csv-line "Latitude"))
         ]))))
(println "done!")