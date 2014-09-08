(ns foodtrucks.parameter
  "A library for parsing raw HTTP query parameters (strings) into correctly typed parameters."
  (:require [clojure.string :refer [blank?]]))

(defmulti parse-parameter
          "Parse a single parameter from its string representation.
          Assumes the string is not blank."
          (fn [param-spec _] (:type param-spec)))

(defmethod parse-parameter :double [_ param]
  (Double/parseDouble param))

(defmethod parse-parameter :string [_ param] param)

(defmethod parse-parameter :integer [_ param]
  (Integer/parseInt param))


(defn parse-parameters
  "Parse string parameters in raw-params and returns a new parameter map with desired types.
  Removes any parameters not specified."
  [parameter-specs raw-params]
  (reduce-kv
    (fn [parsed-params param-key param-spec]
      (if (blank? (param-key raw-params))
        parsed-params
        (assoc parsed-params param-key (parse-parameter param-spec (param-key raw-params)))))
    {}
    parameter-specs))



