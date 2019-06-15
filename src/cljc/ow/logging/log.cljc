(ns ow.logging.log
  #?(:cljs (:require-macros [ow.logging.macros :as m]))
  #?(:clj  (:require [clojure.tools.logging :as log]
                     [ow.logging.core :as c]
                     [ow.logging.macros :as m])
     :cljs (:require [ow.logging.core :as c])))

(defn get-trace
  "Returns the current trace history."
  []
  (get-in c/+callinfo+ [:trace]))

(defn get-trace-root
  "Returns the root/first/topmost entry in the current trace history."
  []
  (get (get-trace) 0))

(defn log-data
  "Returns the current trace info map plus some augmented data (e.g. timestamp)."
  [level msg & [data]]
  (m/with-trace ::log
    (-> c/+callinfo+
        (assoc :level  level
               :msg    msg)
        (update :data merge
                (cond
                  (map? data) (c/pr-str-map-vals data)
                  (nil? data) {}
                  true        {::log-data (pr-str data)})))))

(defn log-str
  "Returns the current trace info map formatted as string."
  [level msg & [data]]
  (pr-str (log-data level name msg data)))

(defn log
  "Prints a log message based on the current trace info map."
  [level msg & [data]]
  #?(:clj  (log/log level (log-str level name msg data))
     :cljs (println (log-str level name msg data))))

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



#_(do (require '[clojure.core.async :as a])
    (let-clj [foo1r (a/chan)
              foo1a (a/chan)
              foo2r (a/chan)
              foo2a (a/chan)]

      (a/go-loop [x (a/<! foo1r)]
        (when-not (nil? x)
          (let [[x] x]
            (m/with-trace inside-foo1
              (warn foo11 "foo1-1" x)
              (Thread/sleep (rand-int 1000))
              (info foo12 "foo1-2" x)
              (a/put! foo1a (->> x inc vector attach))))
          (recur (a/<! foo1r))))

      (a/go-loop [x (a/<! foo2r)]
        (when-not (nil? x)
          (let [x x]
            (warn foo21 "foo2-1" x)
            (Thread/sleep (rand-int 1000))
            (info foo22 "foo2-2" x)
            (a/put! foo2a (->> x inc attach)))
          (recur (a/<! foo2r))))

      (defn bar1 [x]
        (warn bar11 "bar1-1" x)
        (let-clj [r (doall (pvalues (do (a/put! foo1r (->> x inc vector attach))
                                        (let [[res] (a/<!! foo1a)]
                                          res))
                                    (do (a/put! foo2r (->> x inc attach))
                                        (let [res (a/<!! foo2a)]
                                          res))))]
          (info bar12 "bar1-2" x)
          r))

      (defn bar2 [x {:keys [d1] :as mmm} [d2 :as vvv] & {:keys [xyz]}]
        (warn bar21 "bar2-1" x)
        (let-clj [r (doall (pvalues (do (a/put! foo1r (->> x inc vector attach))
                                        (let [[res] (a/<!! foo1a)]
                                          res))
                                    (do (a/put! foo2r (->> x inc attach))
                                        (let [res (a/<!! foo2a)]
                                          res))))]
          (info bar22 "bar2-2" x)
          r))

      (defn baz [x]
        (warn baz1 "baz-1" x)
        (m/with-trace-data {:user "user123"}
          (pvalues (bar1 (inc x))
                   (bar2 (inc x) nil nil)))
        (info baz2 "baz-2" x))

      (baz 123)
      (Thread/sleep 3000)
      (a/close! foo2a)
      (a/close! foo2r)
      (a/close! foo1a)
      (a/close! foo1r)))

#_(defn foo [a b {:keys [c d] :as m} [e f :as v] & [g h]]
  [a b c d e f g h])
