(ns ow.logging.core
  #?(:cljs (:require-macros [ow.logging.core :refer [with-initialized-logging with-instance with-historical-logging-info
                                                     with-logging-info with-checkpoint* with-checkpoint with-data]]))
  (:require [clojure.string :as s]))

(def MAX_INT #?(:clj  Integer/MAX_VALUE
                :cljs (.. js/Number -MAX_SAFE_INTEGER)))

#?(:clj  (def ^:dynamic +logging-info+ {:checkpoints []}))

#?(:cljs (def domain (js/require "domain")))
#?(:cljs (set! (.-stackTraceLimit js/Error) 50))

(defn pr-str-map-vals [m]
  (->> m
       (map (fn [[k v]]
              [k (pr-str v)]))
       (into {})))

(defn current-ste-info []
  #?(:clj (let [st                (some-> (Throwable.) .getStackTrace seq)
                ste               (loop [[ste & st] st]
                                    (let [classname (.getClassName ste)]
                                      (if (or (s/starts-with? classname "ow.logging")
                                              (s/starts-with? classname "clojure"))
                                        (recur st)
                                        ste)))
                [ns fn]           (when ste
                                    (s/split (.getClassName ste) #"\$" 2))
                fn                (when fn
                                    (some->> (s/split fn #"\$")
                                             (remove #(s/starts-with? % "__hide"))
                                             (s/join "$")))
                [file line]       (when ste
                                    [(.getFileName ste) (.getLineNumber ste)])
                [ns fn file line] [(or ns "?") (or fn "?") (or file "?") (or line "?")]]
            {:file file
             :fn   fn
             :line line
             :ns   ns
             :time (java.util.Date.)})

     :cljs (letfn [(dissect [ste]
                     (when-let [[location file line] (some->> ste
                                                              (re-find #"at[\s]+([^\s]+)[\s]+.*\(([^\(\s]+):([0-9]+):[0-9]+\)")
                                                              rest)]
                       (let [location (or (some->> location
                                                   (re-find #"(.+?)\$___hide.*")
                                                   second)
                                          location)]
                         [location file line])))

                   (is-system? [location]
                     (or (s/starts-with? location "ow$logging")
                         (re-matches #"[^\$]+" location)
                         (re-matches #"[A-Z][a-zA-Z0-9-_]+\.cljs\$.+" location)
                         (s/starts-with? location "figwheel$")
                         (s/starts-with? location "cljs$core")
                         #_(s/starts-with? location "Object.cljs$")
                         #_(s/starts-with? location "Function.cljs$")))]

             (let [st  (some-> (js/Error.) (.-stack) (s/split "\n"))
                   [location file line :as ste] (or (loop [[ste & st] st]
                                                      (when ste
                                                        (if-let [[location file line :as dissected] (dissect ste)]
                                                          (if-not (is-system? location)
                                                            dissected
                                                            (recur st))
                                                          (recur st))))
                                                    ["?" "?" "?" "?"])]
               {:file     file
                :fn       location
                :line     line
                :ns       location
                :time     (js/Date.)}))))

(defn make-checkpoint* [name & args]  ;; TODO: create record for checkpoint, to prevent overly verbose printing (e.g. of large arguments)
  (-> {:id   (rand-int MAX_INT)
       :name name}
      (merge (current-ste-info))
      (into  [(when-not (empty? args)
                [:args (map pr-str args)])])))

(defn append-checkpoints [logging-info checkpoints]
  (update logging-info :checkpoints #(-> (concat % checkpoints) vec)))

(defn append-checkpoint [logging-info checkpoint]
  (update logging-info :checkpoints conj checkpoint))

(defn prepend-checkpoints [logging-info checkpoints]
  (update logging-info :checkpoints #(-> (concat checkpoints %) vec)))

(defn prepend-checkpoint [logging-info checkpoint]
  (prepend-checkpoints logging-info [checkpoint]))

;;; TODO: also merge current data, not just the checkpoints:
(defn merge-historical-logging-info [logging-info1 logging-info2]
  (prepend-checkpoints logging-info2 (:checkpoints logging-info1)))

(defn merge-historical-logging-infos [& logging-infos]
  (reduce merge-historical-logging-info logging-infos))

(defn current-logging-info []
  #?(:clj  +logging-info+
     :cljs (let [d (.-active domain)]
             (.-logging_info d))))

(defn get-checkpoints []
  (get-in (current-logging-info) [:checkpoints]))

(defn get-root-checkpoint []
  (get (get-checkpoints) 0))



;;; NOTE: macros always get compiled in java/jvm, even for cljs code, because they are compile time citizens.
;;;   this implies that we need to define them in .clj or .cljc files, not in .cljs files.
;;;   and we need to require them via :require-macros in cljs code.
;;;
;;;   you can define and use macros in one single cljc file in cljs by declaring in the ns expr sth like:
;;;   (:require-macros [macro-ns :refer [macro1 macro2])
;;;   this way it even works seamlessly in clj too, as in clj you can directly refer to the macros by
;;;   their unqualified names.

(defn initialize-logging! [cb]
  (do #?(:clj  (cb)
         :cljs (doto (.create domain)
                 (.run cb)))
      nil))

(defmacro with-initialized-logging [& body]
  `(initialize-logging!
     (fn __hide# [] ~@body)))

(defn create-instance! [cb]
  #?(:clj  (cb)
     :cljs (let [d (doto (.create domain)
                     (.enter))]
             (set! (.-logging_info d) {:checkpoints []})
             (cb))))

(defmacro with-instance [& body]
  `(create-instance!
     (fn __hide# [] ~@body)))

(defn inject-logging-info! [logging-info cb]
  #?(:clj  (binding [+logging-info+ logging-info]
             (cb))
     :cljs (let [d                 (.-active domain)
                 prev-logging-info (.-logging_info d)
                 _                 (set! (.-logging_info d) logging-info)
                 result            (cb)]
             (set! (.-logging_info d) prev-logging-info)
             result)))

(defmacro with-logging-info [logging-info & body]
  `(inject-logging-info! ~logging-info
     (fn __hide# [] ~@body)))

(defmacro with-historical-logging-info [logging-info & body]
  `(inject-logging-info! (merge-historical-logging-info ~logging-info (current-logging-info))
     (fn __hide# [] ~@body)))

(defmacro with-checkpoint* [name [& args] & body]
  `(with-logging-info (append-checkpoint (current-logging-info) (make-checkpoint* '~name ~@args))
     ~@body))

(defmacro with-checkpoint [name & body]
  `(with-checkpoint* ~name []
     ~@body))

(defmacro with-data [data & body]
  `(with-logging-info (update (current-logging-info) :data merge (pr-str-map-vals ~data))
     ~@body))
