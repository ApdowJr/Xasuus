const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
});

test("Authenticated user: cannot read another user's notes", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").get());
});

test("Authenticated user: can create a valid note in their own collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1");
  await assertSucceeds(
    noteRef.set({
      userId: ALICE_UID,
      title: "My First Note",
      content: "Hello world!",
      categoryId: "personal",
      tags: ["quick", "ideas"],
      isPinned: false,
      isFavorite: true,
      isArchived: false,
      color: "purple",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create a note with someone else's userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_spoof");
  await assertFails(
    noteRef.set({
      userId: BOB_UID,
      title: "Spoofed Note",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can query their own notes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
});

test("Authenticated user: can create and update a valid category", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const catRef = aliceDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_1");
  await assertSucceeds(
    catRef.set({
      userId: ALICE_UID,
      name: "Work",
      color: "blue",
      icon: "work",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    catRef.update({
      name: "Work Projects",
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot delete another user's category", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("categories").doc("cat_1").set({
      userId: ALICE_UID,
      name: "Alice Cat",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_1").delete()
  );
});

// Regression coverage for fields added after the first deployed rule revision. A stale deployed
// rule set silently rejected these writes with PERMISSION_DENIED even though the app produced
// valid notes.
test("Authenticated user: can create a note with checklist, attachments, reminder and trash fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_full");
  await assertSucceeds(
    noteRef.set({
      userId: ALICE_UID,
      title: "Shopping",
      content: "Buy milk",
      categoryId: "personal",
      tags: ["home"],
      checklist: [
        { id: "item_1", text: "Milk", isChecked: false },
        { id: "item_2", text: "Bread", isChecked: true },
      ],
      attachments: ["https://example.com/photo.jpg"],
      isPinned: false,
      isFavorite: false,
      isArchived: false,
      isDeleted: false,
      deletedAtMillis: null,
      reminderAtMillis: 1760000000000,
      color: "purple",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: checklist entry must have id, text and isChecked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_bad_checklist");
  await assertFails(
    noteRef.set({
      userId: ALICE_UID,
      title: "Bad checklist",
      checklist: [{ id: "item_1", text: 42, isChecked: false }],
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: title longer than 200 characters is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_long_title");
  await assertFails(
    noteRef.set({
      userId: ALICE_UID,
      title: "x".repeat(201),
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: content longer than 50000 characters is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_long_content");
  await assertFails(
    noteRef.set({
      userId: ALICE_UID,
      title: "Huge note",
      content: "x".repeat(50001),
      createdAt: new Date(),
    })
  );
});
