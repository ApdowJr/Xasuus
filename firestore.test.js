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
