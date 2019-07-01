(ns ow.logging.log
  #?(:cljs (:require-macros [ow.logging.core :as cm]))
  #?(:clj  (:require #_[clojure.tools.logging :as log]
                     [ow.logging.core :as c]
                     [ow.logging.core :as cm])
     :cljs (:require [ow.logging.core :as c])))

(defn log-data [level msg & [data]]
  (cm/with-checkpoint ::log
    (-> (c/current-logging-info)
        (assoc :level  level
               :msg    msg)
        (update :data merge
                (cond
                  (map? data) (c/pr-str-map-vals data)
                  (nil? data) {}
                  true        {::log-data (c/pr-str-val data)})))))

(defn log-str [level msg & [data]]
  (c/pr-str-val (log-data level msg data)))

(defn log [level msg & [data]]
  ;;; TODO: maybe use a logging backend here (at least on the java side)?
  #?(:clj  (println (log-str level msg data))
     :cljs (println (log-str level msg data))))

(defn trace [msg & [data]]
  (log :trace msg data))

(defn debug [msg & [data]]
  (log :debug msg data))

(defn info [msg & [data]]
  (log :info msg data))

(defn warn [msg & [data]]
  (log :warn msg data))

(defn error [msg & [data]]
  (log :error msg data))

(defn fatal [msg & [data]]
  (log :fatal msg data))
