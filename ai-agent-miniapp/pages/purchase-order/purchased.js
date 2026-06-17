const { createListBehavior } = require('./list-behavior')

Page({
  ...createListBehavior(1),
  data: {
    ...createListBehavior(1).data,
    showEditAction: false,
    showInboundAction: true
  }
})
