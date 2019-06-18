(ns ow.logging.log
  #?(:cljs (:require-macros [ow.logging.log :refer [with-checkpoint* with-checkpoint with-data]]))
  #?(:clj  (:require #_[clojure.tools.logging :as log]
                     [ow.logging.core :as c])
     :cljs (:require [ow.logging.core :as c])))

;;; NOTE: macros always get compiled in java/jvm, even for cljs code, because they are compile time citizens.
;;;   this implies that we need to define them in .clj or .cljc files, not in .cljs files.
;;;   and we need to require them via :require-macros in cljs code.
;;;
;;;   you can define and use macros in one single cljc file in cljs by declaring in the ns expr sth like:
;;;   (:require-macros [macro-ns :refer [macro1 macro2])
;;;   this way it even works seamlessly in clj too, as in clj you can directly refer to the macros by
;;;   their unqualified names.

(defmacro with-checkpoint* [name [& args] & body]
  `(c/with-logging-info (update (c/logging-info) :checkpoints conj (c/make-checkpoint* '~name ~@args))
     ~@body))

(defmacro with-checkpoint [name & body]
  `(with-checkpoint* ~name []
     ~@body))

(defmacro with-data [data & body]
  `(c/with-logging-info (update (c/logging-info) :data merge (c/pr-str-map-vals ~data))
     ~@body))



(defn get-checkpoints []
  (get-in (c/logging-info) [:trace]))

(defn get-checkpoints-root []
  (get (get-checkpoints) 0))

(defn log-data [level msg & [data]]
  (with-checkpoint ::log
    (-> (c/logging-info)
        (assoc :level  level
               :msg    msg)
        (update :data merge
                (cond
                  (map? data) (c/pr-str-map-vals data)
                  (nil? data) {}
                  true        {::log-data (pr-str data)})))))

(defn log-str [level msg & [data]]
  (pr-str (log-data level msg data)))

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





#_(defn foo [a b {:keys [c d] :as m} [e f :as v] & [g h]]
  [a b c d e f g h])
