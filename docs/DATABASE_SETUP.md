# Database setup

Pa-Ayo uses Google Cloud Firestore with three collections: `reports`, `news` and `admins`. Collections are created by the first write, so there is no schema to import.

## Local development (no account needed)

```bash
npx -y firebase-tools@15 emulators:start --only firestore --project demo-pa-ayo
```

Keep `FIRESTORE_EMULATOR_HOST=127.0.0.1:8085` in `.env`. The emulator needs Java 21 or newer. Browse the data at <http://127.0.0.1:4000/firestore>.

The emulator does not check indexes. Test the category filter on production once the index below is enabled.

## Production (Firebase Spark plan, free, no credit card)

1. Go to <https://console.firebase.google.com>, choose **Create a project** and stay on the **Spark** plan. Turn Google Analytics off; it is not needed. Do not create a database there.
2. Sign in and deploy, replacing `YOUR_PROJECT_ID`:

   ```bash
   npx -y firebase-tools@15 login
   ```

   ```bash
   npx -y firebase-tools@15 deploy --only firestore --project YOUR_PROJECT_ID
   ```

   This creates the `(default)` database and deploys the deny-all rules and the index. `firebase.json` sets the **Standard** edition and the location **asia-southeast1 (Singapore)**, which cannot be changed later.
3. In **Firestore > Indexes**, wait until the `reports` index (category ascending, createdAt descending) shows **Enabled**.
4. In **Firestore > Rules > Rules Playground**, simulate an unauthenticated `get` on `/reports/any` and confirm it is denied.
5. Open **Project settings > Service accounts > Generate new private key**. Save the file as `secrets/firebase-service-account.json` inside the project. Git ignores it. Never commit or share it.
6. To use this database from your Mac, remove `FIRESTORE_EMULATOR_HOST` from `.env`, set your real `ADMIN_*` values (see "First administrator") and add:

   ```
   FIREBASE_CREDENTIALS_PATH=secrets/firebase-service-account.json
   ```

Do not enable Firebase Storage: it needs the paid Blaze plan. Photos are stored on Cloudinary.

## First administrator

When `admins` is empty, the app creates one account from `ADMIN_USERNAME`, `ADMIN_EMAIL` and `ADMIN_PASSWORD` (8 to 72 characters) and stores only the BCrypt hash. Remove `ADMIN_PASSWORD` from the settings after the first successful sign-in.

## Sample data

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=seed
```

If `reports` is empty, this loads 30 reports: 10 per category, all three statuses, with and without photos, and one report per category at the maximum lengths (100, 200 and 1000 characters).

On the emulator it also loads 6 invented news items (marked `[SAMPLE]`) and one administrator with an expired reset token. These two never reach production.

## Maintenance

- **Quota:** check **Firestore > Usage**. Spark allows 50,000 reads and 20,000 writes per day; one dashboard load reads at most 110 documents.
- **90-day clean-up:** in the console, filter `reports` by `status == RESOLVED`, delete those whose `updatedAt` is more than 90 days old, and delete their photos in Cloudinary (**Media Library > pa-ayo**).
- **Spam or unsuitable photos:** an admin presses **Delete** on the admin page, which also removes the photo. If the site is down, delete the document in the console and its photo in Cloudinary.
