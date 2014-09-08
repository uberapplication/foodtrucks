(ns foodtrucks.sql
  "A small, generic library for building SQL strings."
  (:require [clojure.string :refer [join replace-first]]))

(defn select-clause
  "Build a select clause given a list of field specifications."
  [fields]
  (join ", "
        (map (fn [field]
               ; We quote the alias because Postgres is case insensitive otherwise
               (str (or (:select field) (:name field))
                    " as \""
                    (:name field) "\""))
             fields)))

(defn base-query
  "Create a base SQL query based on a model specification. Currently, all fields are selected."
  [model]
  { :select (select-clause (:fields model)),
    :from (:table model)
    :where []
    :order [],
    :offset nil
    :limit nil
    :params {}
    })

(defn join-query-components [components]
  "Combines the SQL query components into a string, performing substitution
   of the named parameters. Returns a vector of the SQL string
  and a vector of parameters."
  (letfn [(select
            [[_ params]]
            [(str "select " (:select components))
             params])
          (add-from
            [[sql params]]
            [(str sql " from " (:from components))
             params])
          (add-where
            [[sql params]]
            (if (empty? (:where components))
              [sql params]
              [(str sql
                    " where ("
                    (join ") and (" (:where components))
                    ")")
               params]))
          (add-order
            [[sql params]]
            (if (empty? (:order components))
              [sql params]
              [(str sql " order by " (join ", " (:order components)))
               params]))
          (add-offset
            [[sql params]]
            (if (nil? (:offset components))
              [sql params]
              [(str sql " offset :offset")
               (assoc params :offset (:offset components))]
              ))
          (add-limit
            [[sql params]]
            (if (nil? (:limit components))
              [sql params]
              [(str sql " limit :limit")
               (assoc params :limit (:limit components))]))
          ; JDBC does not support named parameters, so
          ; we need to substitute the named params ourselves.
          (replace-named-params
            [[sql params]]
            (let [named-occurences (re-seq #":[A-Za-z\-\d]+" sql)]
              (reduce (fn [[sql param-vec] named-occurence]
                        [(replace-first sql named-occurence "?")
                         (conj param-vec ((keyword (subs named-occurence 1)) params))])
                      [sql []]
                      named-occurences))
            )]
    (-> ["" (:params components)]
        select
        add-from
        add-where
        add-order
        add-offset
        add-limit
        replace-named-params)
    ))
