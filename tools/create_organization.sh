#!/bin/bash
#
# Argos Notary - A new way to secure the Software Supply Chain
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
# Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#


token='eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4MEw5Z1N3SWRPM2R0b2VtYlphWlg2RU4wdEl5R0JZclJ1amNpM1pYOUVBIn0.eyJleHAiOjE2OTIzODI5NDgsImlhdCI6MTY5MjM3OTM0OCwiYXV0aF90aW1lIjoxNjkyMzc5MzQ4LCJqdGkiOiI5M2JlZmRiYy1jNmRmLTQ3M2EtOGQxNi0wN2VhYjBiZGRkNDciLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwODAvcmVhbG1zL29hdXRoLXN0dWIiLCJhdWQiOiJvYXV0aC1zdHViLWNsaWVudCIsInN1YiI6IjhiN2ZiMzVkLTg5YzEtNGZmMS1hZTAwLWYzYTJjNGJiZWFhOSIsInR5cCI6IklEIiwiYXpwIjoib2F1dGgtc3R1Yi1jbGllbnQiLCJub25jZSI6Im5TRWI5YVg3c19rdjlablhQcE5mZm90UkJDRExENklHTXAtZHlVNXVsZ1UiLCJzZXNzaW9uX3N0YXRlIjoiMTg0MjZjMDYtMjE5NC00NWM0LTgzNTgtNWJlNmNmMzhhZWZmIiwiYXRfaGFzaCI6IkUtSTlzNFY1OUpYSUhqa1ZKeFRadnciLCJhY3IiOiIxIiwic2lkIjoiMTg0MjZjMDYtMjE5NC00NWM0LTgzNTgtNWJlNmNmMzhhZWZmIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJMdWtlIFNreXdhbGtlciIsInByZWZlcnJlZF91c2VybmFtZSI6Imx1a2UiLCJnaXZlbl9uYW1lIjoiTHVrZSIsImZhbWlseV9uYW1lIjoiU2t5d2Fsa2VyIiwiZW1haWwiOiJsdWtlQHNreXdhbGtlci5pbXAifQ.GmHCuDpQ1LrmVZe04-AjRLOK_X0DrjvipJsR4YsEoENc-aINuc6Ra4selHlDaHJPzdKjGn92J1fTEfebWzFTlwa1MCo4_HEyUvCnHMg6ckVnIhrOEx9OkW7WAb2B2U4QWMuEnxMmfsSBJffriLU2uxp1efcbao56IXJ_N-daiXMgEuYSI4zjLZ2mb7wVW1VtTdsW2RlYXbMxAqrTkUujDAew1PTreq3kG-DsyCcfX8eYhRg4DpIcV_qcHGyiCJ4NroSObQEiqkzFqrUwZnqWBwbeE8fG7RtzqsP2VL2UhdJNG0d6LU5DScEWGq4mB9DOkcVRW0CI0hB8ZcLPJKaF0w'

curl -vvv -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $token" -d@organization.json http://localhost:8080/api/organizations