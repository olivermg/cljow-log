(ns ow.logging.core-test
  (:require [clojure.test :refer :all ]
            [clojure.core.async :as a]
            [ow.logging.api.alpha :as l]))

(deftest test1
  (let [foo1r (a/chan)
        foo1a (a/chan)
        foo2r (a/chan)
        foo2a (a/chan)
        results (atom [])]

    (letfn [(capture [v]
              (swap! results conj v))]

      (a/go-loop [x (a/<! foo1r)]
        (when-not (nil? x)
          (let [[x] x]
            (l/with-checkpoint inside-foo1
              (capture (l/log-data :info "foo1-1" x))
              (Thread/sleep (rand-int 1000))
              (capture (l/log-data :info "foo1-2" x))
              (a/put! foo1a (->> x inc vector l/attach))))
          (recur (a/<! foo1r))))

      (a/go-loop [x (a/<! foo2r)]
        (when-not (nil? x)
          (let [x x]
            (capture (l/log-data :warn "foo2-1" x))
            (Thread/sleep (rand-int 1000))
            (capture (l/log-data :info "foo2-2" x))
            (a/put! foo2a (->> x inc l/attach)))
          (recur (a/<! foo2r))))

      (defn bar1 [x]
        (capture (l/log-data :warn "bar1-1" x))
        (let [r (doall (pvalues (do (a/put! foo1r (->> x inc vector l/attach))
                                    (let [[res] (a/<!! foo1a)]
                                      res))
                                (do (a/put! foo2r (->> x inc l/attach))
                                    (let [res (a/<!! foo2a)]
                                      res))))]
          (capture (l/log-data :info "bar1-2" x))
          r))

      (defn bar2 [x {:keys [d1] :as mmm} [d2 :as vvv] & {:keys [xyz]}]
        (capture (l/log-data :warn "bar2-1" x))
        (let [r (doall (pvalues (do (a/put! foo1r (->> x inc vector l/attach))
                                    (let [[res] (a/<!! foo1a)]
                                      res))
                                (do (a/put! foo2r (->> x inc l/attach))
                                    (let [res (a/<!! foo2a)]
                                      res))))]
          (capture (l/log-data :info "bar2-2" x))
          r))

      (defn baz [x]
        (capture (l/log-data :warn "baz-1" x))
        (l/with-data {:user "user123"}
          (pvalues (bar1 (inc x))
                   (bar2 (inc x) nil nil)))
        (capture (l/log-data :info "baz-2" x)))

      (baz 123)
      (Thread/sleep 3000)
      (a/close! foo2a)
      (a/close! foo2r)
      (a/close! foo1a)
      (a/close! foo1r)
      (is (not-empty @results)))))
