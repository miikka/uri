(ns round-trip-query-map
  (:require [lambdaisland.uri :as uri]
            [lambdaisland.uri.normalize :as norm]
            [lambdaisland.uri.platform :as platform]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as check]
            [clojure.string :as str]))

(def query-map-gen
  (gen/map gen/keyword
           gen/string))

(check/quick-check 10000
                   (prop/for-all [q query-map-gen]
                     (let [res (-> q
                                   uri/map->query-string
                                   uri/query-string->map)]
                       (or (and (empty? q) (empty? res)) ;; (= nil {})
                           (= q res)))))

(def query-string-part-gen (gen/such-that (complement (partial some #{\& \= \% \space})) gen/string))
(def query-string-gen (gen/fmap
                       (fn [parts]
                         (str/join
                          "&"
                          (map (fn [[k v]] (str k "=" v)) parts)))
                       (gen/list (gen/tuple (gen/such-that seq query-string-part-gen)
                                            (gen/such-that seq query-string-part-gen)))))

(check/quick-check 100
                   (prop/for-all [q query-string-gen]
                     (let [res (-> q
                                   uri/query-string->map
                                   uri/map->query-string
                                   norm/percent-decode)]
                       (or (and (empty? q) (empty? res)) ;; (= nil {})
                           (= q res)))))
{:shrunk {:total-nodes-visited 327, :depth 36, :pass? false, :result false, :result-data nil, :time-shrinking-ms 65, :smallest [" = &= & = "]}, :failed-after-ms 3, :num-tests 13, :seed 1592289824964, :fail ["£pÓn=SzþÂ«s&\fÉ½¼=wÅý¤Ôµ-&È-¼\t½?óø=µ\r÷2¶+T&-ô¤+=`MZR1 -àò!&ÎÉÚÝ=©×ÇÂ&¥=Ø#$/\\jQ&å=¯]3³­ñ&Û¼Ô=ãi´pe&Í=Päzi#V*Wäã"], :result false, :result-data nil, :failing-size 12, :pass? false}

(-> x
    uri/map->query-string
    uri/query-string->map)

(seq (platform/string->byte-seq "\u0000"))

(map long (.getBytes "\b"))

(-> "\u0000=\u0000&\u0001=\u0000&\u0000=\u0000"
    uri/query-string->map
    uri/map->query-string
    norm/percent-decode    )

(norm/percent-encode "\b")

(platform/byte->hex 16)

(uri/map->query-string (uri/query-string->map "x=a+b"))
