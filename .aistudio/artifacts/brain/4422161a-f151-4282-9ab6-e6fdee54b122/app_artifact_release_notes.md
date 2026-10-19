# 📱 MAWA ডিজিটাল খাতা — রিলিজ ও আর্টফ্যাক্ট ওভারভিউ (App Release Artifact)

**অ্যাপ্লিকেশন নাম:** মাওয়া ডিজিটাল খাতা (MAWA Digital Khata)  
**ভার্সন:** 1.0.0 (Production Ready)  
**প্লাটফর্ম:** Android (Kotlin + Jetpack Compose + Material 3)  
**লোকাল ডাটাবেজ:** Android Room Database (SQLite)  
**ক্লাউড ও রিয়েলটাইম ব্যাকএন্ড:** Supabase (PostgreSQL REST API + Realtime WebSockets)  
**বিল্ড স্ট্যাটাস:** সফলভাবে কম্পাইল ও অ্যাসেম্বল সম্পন্ন (APK Generated)  
**এপিকে লোকেশন:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 🌟 প্রধান ফিচারসমূহ (Core Features)

1. **দোকান ও ব্যক্তিগত হিসাব খাতা (Dual Mode Accounting):**
   * **দোকানের খাতা:** বাকি বিক্রি, নগদ বিক্রি, বাকি আদায়, পণ্য ক্রয়, দোকান খরচ এবং ফর্দ (শপিং লিস্ট)।
   * **ব্যক্তিগত খাতা:** পারিবারিক বাজার, চিকিৎসা, সন্তানের পড়াশোনা, ইউটিলিটি বিল ও নিজস্ব আয়ের পৃথক হিসাব।
   * **উভয় মোড (Both):** এক ট্যাপে দোকান ও পারিবারিক হিসাবের মধ্যে সুইচ করা যায়।

2. **লাইভ রিয়েলটাইম ক্লাউড সিঙ্ক (Realtime Supabase Live Sync):**
   * **ইনস্ট্যান্ট পুশ (Instant Field Push):** কোনো কাস্টমার, লেনদেন, পণ্য বা ফর্দ এন্ট্রি করার সাথে সাথে (৫০০ms ডিবাউন্স) স্বয়ংক্রিয়ভাবে সুপাবেজ ক্লাউডে ব্যাকআপ হয়।
   * **তাৎক্ষণিক ডিলিট (Instant Cloud Delete):** কোনো রেকর্ড ডিলিট করলে তা সাথে সাথে সুপাবেজের `mawa_cloud_records` টেবিল থেকেও মুছে যায়।
   * **ব্যাকগ্রাউন্ড সিঙ্ক:** প্রতি ১২ সেকেন্ড পর পর ক্লাউডের নতুন ডাটা রিয়েলটাইমে অ্যাপের সাথে মিলিয়ে নেয়।

3. **স্মার্ট ক্যাশবাক্স ও দৈনিক হিসাব (Daily Cashbox Management):**
   * **সাবেক ক্যাশ (`sabekCash`):** দিনের শুরুতে ক্যাশবাক্সে থাকা আগের জমার হিসাব।
   * **সমাপনী ক্যাশ (`closingCash`):** দিনের শেষে ক্যাশবাক্সে গোনা নগদ টাকা।
   * **স্বয়ংক্রিয় ক্যাশ মেলা ও বেচা হিসাব:**  
     $$\text{নগদ বেচা} = \text{সমাপনী ক্যাশ} + \text{দিনের খরচ} - \text{সকালবেলার সাবেক ক্যাশ} - \text{বাকি আদায়}$$
   * **অক্ষত রাখার নিশ্চয়তা:** ক্লাউড ও লোকাল ডাটা মার্জিংয়ের সময় `sabekCash` ও `closingCash` কোনোটির মানই শূন্য দিয়ে ওভাররাইট হয় না।

4. **স্মার্ট ডিডুপ্লিকেশন ও জেসন মাস্টার ব্যাকআপ (Duplicate Prevention):**
   * **মাস্টার ব্যাকআপ আপডেট:** মোবাইলের ফাইলে প্রতিবার নতুন জেসন তৈরি না করে `mawa_master_backup.json` ফাইলে আপডেট হয়।
   * **ডাবল এন্ট্রি রোধ:** জেসন ফাইল আপলোড করে ক্লাউডে লগইন করলেও তারিখ, কাস্টমার, টাকার পরিমাণ এবং ট্রানজেকশন সিগনেচার মিলিয়ে ডুপ্লিকেট রোধ করা হয়।

---

## 🗄️ সুপাবেজ ডাটাবেজ স্কিমা (Supabase Database Architecture)

সুপাবেজ **SQL Editor**-এ নিচের স্ক্রিপ্টটি রান করলেই অ্যাপের সাথে শতভাগ সামঞ্জস্যপূর্ণ হয়ে যাবে:

```sql
-- ১. mawa_cloud_records টেবিল (ফিল্ডভিত্তিক তাৎক্ষণিক লাইভ সিঙ্ক ও ডিলিট)
CREATE TABLE IF NOT EXISTS public.mawa_cloud_records (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    domain TEXT NOT NULL DEFAULT 'business',
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    data JSONB NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT unique_user_domain_entity UNIQUE (user_id, domain, entity_type, entity_id)
);

-- ২. user_backups টেবিল (সম্পূর্ণ ডাটাবেজ স্ন্যাপশট ব্যাকআপ)
CREATE TABLE IF NOT EXISTS public.user_backups (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    backup_name TEXT NOT NULL,
    backup_data JSONB NOT NULL,
    data JSONB NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT unique_user_backup_name UNIQUE (user_id, backup_name)
);

-- ৩. রিয়েলটাইম লাইভ ট্র্যাকিং সক্রিয় করুন (Realtime WebSockets)
ALTER PUBLICATION supabase_realtime ADD TABLE public.mawa_cloud_records;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_backups;
ALTER TABLE public.mawa_cloud_records REPLICA IDENTITY FULL;
ALTER TABLE public.user_backups REPLICA IDENTITY FULL;

-- ৪. ডাটা সিকিউরিটি পলিসি (Row Level Security - RLS)
ALTER TABLE public.mawa_cloud_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_backups ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can manage their records" ON public.mawa_cloud_records;
CREATE POLICY "Users can manage their records" 
ON public.mawa_cloud_records FOR ALL 
USING (auth.uid() = user_id) 
WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can manage their backups" ON public.user_backups;
CREATE POLICY "Users can manage their backups" 
ON public.user_backups FOR ALL 
USING (auth.uid() = user_id) 
WITH CHECK (auth.uid() = user_id);
```

---

## 🛡️ ইউনিক ইনডেক্স ও `on_conflict` রেজোলিউশন

অ্যাপে মাল্টি-লেভেল অটো-ফলব্যাক ইন্টিগ্রেশন রয়েছে:
* `on_conflict=user_id,domain,entity_type,entity_id`
* ফলব্যাক ১: `on_conflict=user_id,entity_type,entity_id`
* ফলব্যাক ২: `on_conflict=user_id,entity_id`
* ফলব্যাক ৩: ফিল্টারড `PATCH` অপারেশন।
* ফলে সার্ভারে যে কোনো কনস্ট্রেইন্ট থাকুক না কেন, `42P10` এরর ছাড়াই নিরবচ্ছিন্নভাবে Upsert কার্যকর থাকে।

---

## 📦 এপিকে ইনস্টলেশন ও ব্যবহার নির্দেশিকা

1. এআই স্টুডিওর **Settings / Export** মেনু থেকে প্রোজেক্টের জিপ ফাইল ডাউনলোড করতে পারেন অথবা সরাসরি ব্রাউজারের স্ট্রিমিং এমুলেটরে অ্যাপটি ব্যবহার করতে পারেন।
2. জেনারেট হওয়া ডিবাগ এপিকে ফাইল: `app/build/outputs/apk/debug/app-debug.apk`।
3. অ্যাপের **ব্যাকআপ ও রিস্টোর** স্ক্রিন থেকে **"সুপাবেজে লাইভ রিয়েলটাইম করতে করণীয়"** কার্ডের SQL স্ক্রিপ্টটি ১-ট্যাপে ক্লিপবোর্ডে কপি করে সুপাবেজে পেস্ট করা যাবে।
