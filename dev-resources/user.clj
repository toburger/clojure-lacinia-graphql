(ns user
  (:require
   [clojure-game-geek.system :as system]
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.java.browse :refer [browse-url]]
   [clojure.walk :as walk]
   [com.stuartsierra.component :as component])
  (:import (clojure.lang IPersistentMap)))

(defn simplify
  "Converts all ordered maps nested within the map into standard has maps, and
   sequences into vertors, which makes for easier contants in the tests, and
   elmininates ordering problems."
  [m]
  (walk/postwalk
   (fn [node]
     (cond (instance? IPersistentMap node)
           (into {} node)

           (seq? node)
           (vec node)

           :else
           node))
   m))

(defonce system nil)

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)
      simplify))

(defn start
  []
  (alter-var-root #'system (fn [_]
                             (-> (system/new-system)
                                 component/start-system)))
  (browse-url "http://localhost:8888/")
  :started)

(defn stop
  []
  (when (some? system)
    (component/stop-system system)
    (alter-var-root #'system component/stop-system))
  :stopped)

(comment
  (start)
  (stop)
)